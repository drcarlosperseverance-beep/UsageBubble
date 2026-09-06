package com.yamil.usagebubble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private TextView bubble;
    private WebView reader;
    private WindowManager.LayoutParams bubbleParams;
    private WindowManager.LayoutParams readerParams;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int pagePollCount;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            updateBubble();
        }
    };

    private final Runnable periodicRefresh = new Runnable() {
        @Override public void run() {
            refreshPage();
            long delay = TimeUnit.MINUTES.toMillis(UsageStore.intervalMinutes(OverlayService.this));
            handler.postDelayed(this, delay);
        }
    };

    private final Runnable updateAge = new Runnable() {
        @Override public void run() {
            updateBubble();
            handler.postDelayed(this, 60_000L);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this, "usage")
                .setContentTitle("Usage Bubble activa")
                .setContentText("Actualiza los porcentajes automáticamente")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(pending)
                .build();
        startForeground(10, notification);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        showBubble();
        createBackgroundReader();

        IntentFilter filter = new IntentFilter(UsageWebViewActivity.ACTION_USAGE_UPDATED);
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
        periodicRefresh.run();
        updateAge.run();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (reader != null) refreshPage();
        return START_STICKY;
    }

    private void showBubble() {
        if (!Settings.canDrawOverlays(this)) return;
        bubble = new TextView(this);
        bubble.setTextColor(Color.WHITE);
        bubble.setTextSize(12);
        bubble.setGravity(Gravity.CENTER);
        bubble.setBackgroundColor(Color.rgb(36, 87, 214));
        bubble.setPadding(14, 10, 14, 10);
        updateBubble();

        int type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        bubbleParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = getSharedPreferences("usage", MODE_PRIVATE).getInt("bubble_x", 24);
        bubbleParams.y = getSharedPreferences("usage", MODE_PRIVATE).getInt("bubble_y", 180);

        bubble.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean moved;

            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = bubbleParams.x;
                        initialY = bubbleParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - initialTouchX);
                        int dy = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(dx) > 8 || Math.abs(dy) > 8) moved = true;
                        bubbleParams.x = initialX + dx;
                        bubbleParams.y = initialY + dy;
                        windowManager.updateViewLayout(bubble, bubbleParams);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (moved) {
                            getSharedPreferences("usage", MODE_PRIVATE).edit()
                                    .putInt("bubble_x", bubbleParams.x)
                                    .putInt("bubble_y", bubbleParams.y)
                                    .apply();
                        } else {
                            openPanel();
                        }
                        return true;
                    default:
                        return true;
                }
            }
        });
        windowManager.addView(bubble, bubbleParams);
    }

    private void createBackgroundReader() {
        if (!Settings.canDrawOverlays(this)) return;
        reader = new WebView(this);
        WebSettings settings = reader.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(reader, true);
        reader.addJavascriptInterface(new BackgroundBridge(), "UsageBubble");
        reader.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                pagePollCount = 0;
                pollLoadedPage();
            }
        });
        reader.setAlpha(0.01f);
        readerParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        readerParams.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(reader, readerParams);
    }

    private void refreshPage() {
        if (reader == null) return;
        String url = getSharedPreferences("usage", MODE_PRIVATE)
                .getString("url", MainActivity.DEFAULT_URL);
        reader.loadUrl(url);
    }

    private void pollLoadedPage() {
        if (reader == null || pagePollCount >= 30) return;
        pagePollCount++;
        reader.evaluateJavascript(
                "UsageBubble.receive(document.body ? document.body.innerText : '')", null);
        handler.postDelayed(this::pollLoadedPage, 1000L);
    }

    private class BackgroundBridge {
        @JavascriptInterface public void receive(String raw) {
            String[] values = UsageParser.parse(raw);
            if (!"--".equals(values[0]) || !"--".equals(values[1])) {
                UsageStore.save(OverlayService.this, values[0], values[1]);
                handler.post(OverlayService.this::updateBubble);
            }
        }
    }

    private void openPanel() {
        Intent intent = new Intent(this, UsageWebViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("url", getSharedPreferences("usage", MODE_PRIVATE)
                .getString("url", MainActivity.DEFAULT_URL));
        startActivity(intent);
    }

    private void updateBubble() {
        if (bubble == null) return;
        String age = ageText(UsageStore.updatedAt(this));
        bubble.setText("5h " + UsageStore.fiveHour(this)
                + "\nS " + UsageStore.weekly(this)
                + "\n" + age);
    }

    private String ageText(long updatedAt) {
        if (updatedAt <= 0L) return "sin datos";
        long minutes = Math.max(0L, TimeUnit.MILLISECONDS.toMinutes(
                System.currentTimeMillis() - updatedAt));
        return minutes == 0L ? "ahora" : "hace " + minutes + "m";
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "usage", "Usage Bubble", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        try { unregisterReceiver(receiver); } catch (IllegalArgumentException ignored) {}
        if (reader != null) {
            reader.removeJavascriptInterface("UsageBubble");
            if (windowManager != null) windowManager.removeView(reader);
            reader.destroy();
        }
        if (windowManager != null && bubble != null) windowManager.removeView(bubble);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
