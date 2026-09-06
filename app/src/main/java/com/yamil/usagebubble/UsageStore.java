package com.yamil.usagebubble;

import android.content.Context;

/** Stores the last values read from the usage page. */
public final class UsageStore {
    private static final String PREFS = "usage";
    private UsageStore() {}

    public static void save(Context context, String fiveHour, String weekly) {
        android.content.SharedPreferences.Editor editor = context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        if (fiveHour != null && !"--".equals(fiveHour)) editor.putString("five_hour", fiveHour);
        if (weekly != null && !"--".equals(weekly)) editor.putString("weekly", weekly);
        editor.putLong("updated", System.currentTimeMillis()).apply();
    }

    public static String fiveHour(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("five_hour", "--");
    }

    public static String weekly(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("weekly", "--");
    }

    public static long updatedAt(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getLong("updated", 0L);
    }

    public static void setIntervalMinutes(Context context, int minutes) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt("interval_minutes", Math.max(2, minutes)).apply();
    }

    public static int intervalMinutes(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt("interval_minutes", 5);
    }
}
