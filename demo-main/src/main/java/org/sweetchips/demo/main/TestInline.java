package org.sweetchips.demo.main;

public class TestInline extends AbstractTest {

    @Override
    protected final void onTest() {
        log("checkReturn", getClass().getName().equals(toString()));
    }

    @Override
    public final String toString() {
        return toStringPrivate();
    }

    private String toStringPrivate() {
        return toStringSynchronized();
    }

    synchronized String toStringSynchronized() {
        return toStringStatic(null, this);
    }

    static String toStringStatic(Object obj, TestInline self) {
        return self.getClass().getName();
    }
}
