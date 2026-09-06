package com.yamil.usagebubble;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsageWebViewActivity extends Activity {
    private static final Pattern FIVE_HOUR = Pattern.compile("(?is)(?:5\\s*[- ]?hour|5h)[^%]{0,120}?(\\d{1,3})\\s*%");
    private static final Pattern WEEKLY = Pattern.compile("(?is)weekly[^%]{0,120}?(\\d{1,3})\\s*%");
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        WebView web = new WebView(this);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
        web.addJavascriptInterface(new ReaderBridge(), "UsageBubble");
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String page) {
                view.evaluateJavascript("UsageBubble.receive(document.body.innerText)", null);
            }
        });
        setContentView(web);
        String url = getIntent().getStringExtra("url");
        web.loadUrl(url == null ? MainActivity.DEFAULT_URL : url);
    }

    private class ReaderBridge {
        @JavascriptInterface public void receive(String raw) {
            if (raw == null) return;
            String text = raw.replace("\\n", " ").replace("\\r", " ");
            String five = find(FIVE_HOUR, text);
            String weekly = find(WEEKLY, text);
            if (!"--".equals(five) || !"--".equals(weekly)) {
                UsageStore.save(UsageWebViewActivity.this, five, weekly);
                sendBroadcast(new Intent("com.yamil.usagebubble.USAGE_UPDATED"));
            }
        }
    }

    private String find(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1) + "%" : "--";
    }
}
