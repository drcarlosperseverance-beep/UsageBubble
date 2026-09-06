package com.yamil.usagebubble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private TextView bubble;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) { updateBubble(); }
    };

    @Override public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this, "usage")
                .setContentTitle("Usage Bubble activa")
                .setContentText("Toca la burbuja para abrir el panel de uso")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(pending)
                .build();
        startForeground(10, notification);
        showBubble();
        IntentFilter filter = new IntentFilter("com.yamil.usagebubble.USAGE_UPDATED");
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
    }

    private void showBubble() {
        if (!Settings.canDrawOverlays(this)) return;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        bubble = new TextView(this);
        updateBubble();
        bubble.setTextColor(Color.WHITE);
        bubble.setTextSize(12);
        bubble.setGravity(Gravity.CENTER);
        bubble.setBackgroundColor(Color.rgb(36, 87, 214));
        bubble.setPadding(12, 8, 12, 8);
        bubble.setOnClickListener(v -> {
            Intent i = new Intent(this, UsageWebViewActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("url", getSharedPreferences("usage", MODE_PRIVATE)
                    .getString("url", MainActivity.DEFAULT_URL));
            startActivity(i);
        });
        int type = android.os.Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.END;
        p.x = 18; p.y = 180;
        windowManager.addView(bubble, p);
    }

    private void updateBubble() {
        if (bubble != null) bubble.setText("5h " + UsageStore.fiveHour(this)
                + "\nW " + UsageStore.weekly(this));
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("usage", "Usage Bubble",
                NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override public void onDestroy() {
        unregisterReceiver(receiver);
        if (windowManager != null && bubble != null) windowManager.removeView(bubble);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
