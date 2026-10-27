package com.example.donutauction.util;

import java.util.Locale;

public final class Fmt {
    private Fmt() {}

    /** Countdown text: s.mmm below the precision threshold, m:ss otherwise. */
    public static String time(long ms, int preciseBelowSec) {
        if (ms < 0) ms = 0;
        if (ms < preciseBelowSec * 1000L) {
            return String.format(Locale.ROOT, "%d.%03d s", ms / 1000, ms % 1000);
        }
        long s = (ms + 999) / 1000;
        if (s >= 3600) return String.format(Locale.ROOT, "%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
        return String.format(Locale.ROOT, "%d:%02d", s / 60, s % 60);
    }

    public static String amount(double v) {
        if (Double.isNaN(v)) return "-";
        if (v == Math.rint(v)) return String.format(Locale.US, "%,.0f", v);
        return String.format(Locale.US, "%,.2f", v);
    }

    /** Plain number for the clipboard (no separators). */
    public static String raw(double v) {
        if (v == Math.rint(v)) return String.valueOf((long) v);
        return String.format(Locale.US, "%.2f", v);
    }

    public static String delay(long ms) {
        return ms % 1000 == 0 ? (ms / 1000) + "s" : ms + "ms";
    }

    public static String age(long ms) {
        if (ms < 0) return "never";
        if (ms < 1000) return "<1s ago";
        if (ms < 60_000) return (ms / 1000) + "s ago";
        return (ms / 60_000) + "m ago";
    }
}
