package com.yamil.usagebubble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;

public class UsageWebViewActivity extends Activity {
    public static final String ACTION_USAGE_UPDATED = "com.yamil.usagebubble.USAGE_UPDATED";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView web;
    private int pollCount;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
        web.addJavascriptInterface(new ReaderBridge(), "UsageBubble");
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String page) {
                pollCount = 0;
                pollPage();
            }
        });
        setContentView(web);
        String url = getIntent().getStringExtra("url");
        web.loadUrl(url == null ? MainActivity.DEFAULT_URL : url);
    }

    private void pollPage() {
        if (web == null || pollCount >= 30) return;
        pollCount++;
        web.evaluateJavascript(
                "UsageBubble.receive(document.body ? document.body.innerText : '')", null);
        handler.postDelayed(this::pollPage, 1000L);
    }

    private class ReaderBridge {
        @JavascriptInterface public void receive(String raw) {
            if (raw == null) return;
            String[] values = UsageParser.parse(raw);
            String five = values[0];
            String weekly = values[1];
            if (!"--".equals(five) || !"--".equals(weekly)) {
                UsageStore.save(UsageWebViewActivity.this, five, weekly);
                Intent update = new Intent(ACTION_USAGE_UPDATED);
                update.setPackage(getPackageName());
                sendBroadcast(update);
            }
        }
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (web != null) {
            web.removeJavascriptInterface("UsageBubble");
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
