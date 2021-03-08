package org.sweetchips.utility;

import java.util.Map;

final class PairEntry<K, V> implements Map.Entry<K, V> {

    private final K mKey;
    private final V mValue;

    PairEntry(K key, V value) {
        mKey = key;
        mValue = value;
    }

    @Override
    public K getKey() {
        return mKey;
    }

    @Override
    public V getValue() {
        return mValue;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        K k = mKey;
        V v = mValue;
        return (k != null ? k.hashCode() : 0) ^ (v != null ? v.hashCode() : 0);

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PairEntry) {
            PairEntry<?, ?> pair = (PairEntry<?, ?>) obj;
            K k = mKey;
            V v = mValue;
            Object l = pair.mKey;
            Object r = pair.mValue;
            return (k == null && l == null || k != null && k.equals(l))
                    && (v == null && r == null || v != null && v.equals(r));
        }
        return false;
    }
}
