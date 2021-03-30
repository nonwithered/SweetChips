package org.sweetchips.demo.main;

final class TestInline extends AbstractTest {

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

    private synchronized String toStringSynchronized() {
        return toStringStatic(null, this, null);
    }

    private static String toStringStatic(Object before, TestInline self, Object after) {
        checkInline('0', "1", 2, 3.0f, "4", 5, 6L);
        return self.getClass().getName();
    }

    private static void checkInline(char v0, String v1, int v2, float v3, String v4, int v5, long v6) {
        print(v4, v5, v5);
        print(v1, v2, v2);
    }

    private static void print(String a, int b, double c) {
        print(c);
        print(b);
        print(a);
    }

    private static void print(Object object) {
        System.out.println(object);
    }
}
