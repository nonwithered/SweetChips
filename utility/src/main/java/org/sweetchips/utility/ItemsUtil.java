package org.sweetchips.utility;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;

public interface ItemsUtil {

    static <K, V> Map.Entry<K, V> newPairEntry(K k, V v) {
        return new PairEntry<>(k, v);
    }

    static <E> void checkAndAdd(Collection<E> collection, E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        if (collection == null) {
            return;
        }
        collection.add(e);
    }

    static <E> void checkAndAddFirst(Deque<E> deque, E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        if (deque == null) {
            return;
        }
        deque.addFirst(e);
    }
}
