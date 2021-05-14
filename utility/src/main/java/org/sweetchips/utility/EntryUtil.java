package org.sweetchips.utility;

import java.util.Map;

public interface EntryUtil {

    static <K, V> Map.Entry<K, V> newPairEntry(K k, V v) {
        return new PairEntry<>(k, v);
    }
}
