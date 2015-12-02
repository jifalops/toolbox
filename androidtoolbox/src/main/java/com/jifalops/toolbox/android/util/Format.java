package com.jifalops.toolbox.android.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class Format {
    private Format() {}

    public static final String DATE = "yyyy-MM-dd";
    public static final String TIME_24 = "HH:mm:ss";
    public static final String TIME_24_MILLIS = "HH:mm:ss.SSS";
    public static final String DATETIME = DATE + " " + TIME_24;
    public static final String DATETIME_MILLIS = DATE + " " + TIME_24_MILLIS;
    public static final String DATETIME_FILENAME = DATE + "_" + "HH-mm-ss";

    // Converting from nanoseconds
    public static final long NANOS_PER_MICRO    = 1000L;
    public static final long NANOS_PER_MILLI    = 1000   * NANOS_PER_MICRO;
    public static final long NANOS_PER_SECOND   = 1000   * NANOS_PER_MILLI;
    public static final long NANOS_PER_MINUTE   = 60     * NANOS_PER_SECOND;
    public static final long NANOS_PER_HOUR     = 60     * NANOS_PER_MINUTE;
    public static final long NANOS_PER_DAY      = 24     * NANOS_PER_HOUR;
    public static final long NANOS_PER_WEEK     = 7      * NANOS_PER_DAY;

    public static String duration(long nanos) {
        long weeks = nanos / NANOS_PER_WEEK;
            nanos -= weeks * NANOS_PER_WEEK;
        long days = nanos / NANOS_PER_DAY;
            nanos -= days * NANOS_PER_DAY;
        long hours = nanos / NANOS_PER_HOUR;
            nanos -= hours * NANOS_PER_HOUR;
        long minutes = nanos / NANOS_PER_MINUTE;
            nanos -= minutes * NANOS_PER_MINUTE;
        double seconds = (double) nanos / NANOS_PER_SECOND;

        String label = "";
        if (weeks   > 0) label += weeks   + "w ";
        if (days    > 0) label += days    + "d ";
        if (hours   > 0) label += hours   + "h ";
        if (minutes > 0) label += minutes + "m ";

        if (seconds >= 1.0) {
            label += String.format(Locale.US, "%.9fs", seconds);
        } else if (seconds >= 1e-3) {
            label += String.format(Locale.US, "%.6fms", seconds * 1e3);
        } else if (seconds >= 1e-6) {
            label += String.format(Locale.US, "%.3fus", seconds * 1e6);
        } else {
            label += (int)(seconds * 1e9) + "ns";
        }

        return label;
    }

    public static String duration(double seconds) {
        return duration((long) (seconds * 1e9));
    }


    public static String time(long millisSinceEpoch, String format) {
        return new SimpleDateFormat(format, Locale.US).format(new Date(millisSinceEpoch));
    }

    public static String time(String format) {
        return time(System.currentTimeMillis(), format);
    }
}
