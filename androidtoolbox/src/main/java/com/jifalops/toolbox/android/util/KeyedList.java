package com.jifalops.toolbox.android.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Turn a List into a map->listitem according to a certain member of that list's type.
 * For example, if you had a list of log entries, where each entry contained an integer for its
 * importance, you could use this class to divide the log entries by importance.
 * i.e. group-by instead of order-by.
 */
public class KeyedList<K, V> extends HashMap<K, List<V>> {
    public interface KeyLookup<K, V> {
        K getKey(V item);
    }

    public KeyedList(List<V> list, KeyLookup<K, V> keyLookup) {
        List<V> tmp;
        K key;
        for (V v : list) {
            key = keyLookup.getKey(v);
            tmp = get(key);
            if (tmp == null) {
                tmp = new ArrayList<>();
                put(key, tmp);
            }
            tmp.add(v);
        }
    }
}
