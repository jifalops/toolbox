package com.jifalops.toolbox.android.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Maintains a list of messages. Each message is automatically timestamped with
 * {@link System#currentTimeMillis()} and can be tagged with an integer for the
 * priority of that message.
 */
public class SimpleLog {
    public static class LogEntry implements Comparable<LogEntry> {
        public final long time = System.currentTimeMillis();
        public final int priority;
        public final String msg;
        private LogEntry(int priority, String msg) {
            this.priority = priority;
            this.msg = msg;
        }

        @Override
        public int compareTo(LogEntry another) {
            if (time < another.time) return -1;
            if (time > another.time) return 1;
            return 0;
        }
    }

    private final List<LogEntry> log = new ArrayList<>();

    public int size() {
        return log.size();
    }

    public void add(String msg) { add(0, msg); }
    public void add(int priority, String msg) {
        log.add(new LogEntry(priority, msg));
    }

    public LogEntry get(int index) {
        return log.get(index);
    }

    public Iterator<LogEntry> iterator() {
        return log.iterator();
    }

    public List<LogEntry> getBypriority(int priority, boolean includeGreaterpriority) {
        List<LogEntry> list = new ArrayList<>();
        if (includeGreaterpriority) {
            for (LogEntry li : log) {
                if (li.priority >= priority) list.add(li);
            }
        } else {
            for (LogEntry li : log) {
                if (li.priority == priority) list.add(li);
            }
        }
        return list;
    }

    /** @return all items before or after the specified time */
    public List<LogEntry> getByTime(long systemTimeMillis, boolean before) {
        List<LogEntry> list = new ArrayList<>();
        if (before) {
            for (LogEntry li : log) {
                if (li.time < systemTimeMillis) list.add(li);
//                else break; // log is always chronological.
            }
        } else {
            for (LogEntry li : log) {
                if (li.time > systemTimeMillis) list.add(li);
            }
        }
        return list;
    }

    /** Combines logs in chronological order. */
    public static SimpleLog combine(SimpleLog... logs) {
        SimpleLog combined = new SimpleLog();
        for (SimpleLog log : logs) {
            combined.log.addAll(log.log);
        }
        Collections.sort(combined.log);
        return combined;
    }
}
