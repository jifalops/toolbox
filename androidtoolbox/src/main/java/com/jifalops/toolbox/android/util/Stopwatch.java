package com.jifalops.toolbox.android.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Stopwatch {
    static final String TAG = Stopwatch.class.getSimpleName();

    private long start, end;
    private final List<Long> laps = new ArrayList<>();

    public void start() {
        laps.clear();
        end = 0;
        start = System.nanoTime();
    }

    public void lap() {
        laps.add(System.nanoTime());
    }

    /** @return the total elapsed time in nanoseconds since calling start */
    public long stop() {
        end = System.nanoTime();
        return end - start;
    }

    public long[] getElapsed() {
        long[] elapsed = new long[laps.size() + 1];
        int len = laps.size();
        if (len == 0) {
            elapsed[0] = end - start;
        } else {
            elapsed[0] = laps.get(0) - start;
            for (int i = 1; i < len; i++) {
                elapsed[i] = laps.get(i) - laps.get(i - 1);
            }
        }
        return elapsed;
    }

    /**
     * Preliminary testing shows that stopwatch takes a few extra milliseconds after 1000 calls,
     * and adds about 20 microseconds of overhead for one call when compared to using
     * System.nanoTime() with local variables.
     */
    public static void test() {
        Log.d(TAG, "Testing Stopwatch...");
        Stopwatch timer = new Stopwatch();
        long elapsed = 0, tmp = 0;
        for (int i = 0; i < 1e3; ++i) {
            long start, end;
            start = System.nanoTime();
            tmp ^= System.nanoTime();
            end = System.nanoTime();
            elapsed += end - start;
        }
        Log.d(TAG, "Calling System.nanoTime() 1000 times without Stopwatch took " + Format.duration(elapsed));

        elapsed = 0;
        for (int i = 0; i < 1e3; ++i) {
            timer.start();
            tmp ^= System.nanoTime();
            elapsed += timer.stop();
        }
        Log.d(TAG, "Calling System.nanoTime() 1000 times with Stopwatch took " + Format.duration(elapsed));

        long end, start = System.nanoTime();
        tmp ^= System.nanoTime();
        end = System.nanoTime();
        Log.d(TAG, "Calling System.nanoTime() around 1 instruction without Stopwatch took " + Format.duration(end - start));

        timer.start();
        tmp ^= System.nanoTime();
        elapsed = timer.stop();
        Log.d(TAG, "Calling System.nanoTime() around 1 instruction with Stopwatch took " + Format.duration(elapsed));
        Log.v(TAG, "tmp = " + tmp);

    }
}
