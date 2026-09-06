package com.yamil.usagebubble;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    public static final String DEFAULT_URL = "https://chatgpt.com/codex/cloud/settings/analytics#usage";
    private EditText url;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 36, 36, 24);

        TextView title = new TextView(this);
        title.setText("Usage Bubble");
        title.setTextSize(26);
        title.setTextColor(Color.rgb(25, 25, 25));
        root.addView(title);

        TextView help = new TextView(this);
        help.setText("Muestra el último porcentaje leído del panel de uso de ChatGPT Work/Codex.");
        help.setPadding(0, 14, 0, 18);
        root.addView(help);

        url = new EditText(this);
        url.setSingleLine(true);
        url.setText(getPreferences(MODE_PRIVATE).getString("url", DEFAULT_URL));
        url.setHint("URL del panel de uso");
        root.addView(url);

        EditText interval = new EditText(this);
        interval.setSingleLine(true);
        interval.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        interval.setText("15");
        interval.setHint("Intervalo en minutos");
        root.addView(interval);

        CheckBox auto = new CheckBox(this);
        auto.setText("Actualizar automáticamente mientras la burbuja esté activa");
        auto.setChecked(true);
        root.addView(auto);

        Button login = new Button(this);
        login.setText("Abrir panel e iniciar sesión");
        login.setOnClickListener(v -> openReader());
        root.addView(login);

        Button start = new Button(this);
        start.setText("Activar burbuja flotante");
        start.setOnClickListener(v -> startBubble(auto.isChecked()));
        root.addView(start);

        Button stop = new Button(this);
        stop.setText("Desactivar burbuja");
        stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class)));
        root.addView(stop);

        TextView values = new TextView(this);
        values.setPadding(0, 22, 0, 0);
        values.setTextSize(18);
        values.setText(currentValues());
        root.addView(values);

        setContentView(root);
    }

    private String currentValues() {
        return "Último dato guardado:\n5 horas: " + UsageStore.fiveHour(this)
                + "\nSemanal: " + UsageStore.weekly(this);
    }

    private void openReader() {
        saveUrl();
        Intent i = new Intent(this, UsageWebViewActivity.class);
        i.putExtra("url", url.getText().toString());
        startActivity(i);
    }

    private void startBubble(boolean auto) {
        saveUrl();
        if (!Settings.canDrawOverlays(this)) {
            Intent settings = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(settings);
            Toast.makeText(this, "Activa 'mostrar sobre otras aplicaciones' y vuelve a pulsar el botón.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(this, OverlayService.class);
        i.putExtra("url", url.getText().toString());
        i.putExtra("auto", auto);
        startForegroundService(i);
        Toast.makeText(this, "Burbuja activada", Toast.LENGTH_SHORT).show();
    }

    private void saveUrl() {
        String value = url.getText().toString().trim();
        getPreferences(MODE_PRIVATE).edit().putString("url", value).apply();
        getSharedPreferences("usage", MODE_PRIVATE).edit().putString("url", value).apply();
    }
}
