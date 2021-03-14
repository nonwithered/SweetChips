package org.sweetchips.plugin.android;

import groovy.lang.Closure;

public class ClosureExtension<V> extends Closure<V> {

    public ClosureExtension(Object owner) {
        super(owner);
    }

    public V call(Closure<V> closure) {
        closure.setDelegate(this);
        return closure.call(this);
    }
}
