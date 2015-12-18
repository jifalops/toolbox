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

    public static final long BYTES_PER_KB = 1000;
    public static final long BYTES_PER_MB = 1000 * BYTES_PER_KB;
    public static final long BYTES_PER_GB = 1000 * BYTES_PER_MB;
    public static final long BYTES_PER_TB = 1000 * BYTES_PER_GB;

    public static final long BYTES_PER_KiB = 1024;
    public static final long BYTES_PER_MiB = 1024 * BYTES_PER_KiB;
    public static final long BYTES_PER_GiB = 1024 * BYTES_PER_MiB;
    public static final long BYTES_PER_TiB = 1024 * BYTES_PER_GiB;
    
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


    public static String dateTime(long millisSinceEpoch, String format) {
        return new SimpleDateFormat(format, Locale.US).format(new Date(millisSinceEpoch));
    }

    public static String dateTime(String format) {
        return dateTime(System.currentTimeMillis(), format);
    }

    public static String bytes(long bytes) {
        return bytes(bytes, true);
    }

    public static String bytes(long bytes, boolean base10) {
        String label;
        double value = bytes;
        int decimalPlaces;
        if (base10) {
            if (bytes < BYTES_PER_KB) {
                label = "B";
                decimalPlaces = 0;
            } else if (bytes < BYTES_PER_MB) {
                label = "KB";
                value /= BYTES_PER_KB;
                decimalPlaces = 3;
            } else if (bytes < BYTES_PER_GB) {
                label = "MB";
                value /= BYTES_PER_MB;
                decimalPlaces = 6;
            } else if (bytes < BYTES_PER_TB) {
                label = "GB";
                value /= BYTES_PER_GB;
                decimalPlaces = 9;
            } else {
                label = "TB";
                value /= BYTES_PER_TB;
                decimalPlaces = 12;
            }
        } else {
            if (bytes < BYTES_PER_KiB) {
                label = "B";
                decimalPlaces = 0;
            } else if (bytes < BYTES_PER_MiB) {
                label = "KiB";
                value /= BYTES_PER_KiB;
                decimalPlaces = 3;
            } else if (bytes < BYTES_PER_GiB) {
                label = "MiB";
                value /= BYTES_PER_MiB;
                decimalPlaces = 6;
            } else if (bytes < BYTES_PER_TiB) {
                label = "GiB";
                value /= BYTES_PER_GiB;
                decimalPlaces = 9;
            } else {
                label = "TiB";
                value /= BYTES_PER_TiB;
                decimalPlaces = 12;
            }
        }
        return String.format(Locale.US, "%." + decimalPlaces + "f %s", value, label);
    }
}
