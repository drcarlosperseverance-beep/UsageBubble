package com.yamil.usagebubble;

import android.content.Context;

/** Stores the last values read from the usage page. */
public final class UsageStore {
    private static final String PREFS = "usage";
    private UsageStore() {}

    public static void save(Context context, String fiveHour, String weekly) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString("five_hour", fiveHour)
                .putString("weekly", weekly)
                .putLong("updated", System.currentTimeMillis())
                .apply();
    }

    public static String fiveHour(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("five_hour", "--");
    }

    public static String weekly(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("weekly", "--");
    }
}
