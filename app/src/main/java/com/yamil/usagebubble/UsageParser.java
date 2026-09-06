package com.yamil.usagebubble;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses percentages from the visible text of the ChatGPT usage page. */
public final class UsageParser {
    private static final Pattern FIVE_HOUR = Pattern.compile(
            "(?is)(?:l[ií]mite\\s+de\\s+uso\\s+de\\s+5\\s+horas|5\\s*[- ]?hours?|5h)" +
                    "[^%]{0,240}?(\\d{1,3})\\s*%");
    private static final Pattern WEEKLY = Pattern.compile(
            "(?is)(?:l[ií]mite\\s+de\\s+uso\\s+semanal|weekly(?:\\s+usage)?(?:\\s+limit)?)" +
                    "[^%]{0,240}?(\\d{1,3})\\s*%");

    private UsageParser() {}

    public static String[] parse(String text) {
        String safe = text == null ? "" : text.replace('\n', ' ').replace('\r', ' ');
        return new String[]{find(FIVE_HOUR, safe), find(WEEKLY, safe)};
    }

    private static String find(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) + "%" : "--";
    }
}
