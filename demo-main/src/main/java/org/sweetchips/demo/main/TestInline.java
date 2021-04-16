package org.sweetchips.demo.main;

import org.sweetchips.annotations.Inline;

final class TestInline extends AbstractTest {

    private static boolean sCheckAnnotation = false;

    @Override
    protected final void onTest() {
        log("checkReturn", getClass().getName().equals(toString()));
        sCheckAnnotation = true;
        CheckAnnotation.clinit();
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
        CheckAnnotation.checkInline('0', "1", 2, 3.0f, "4", 5, 6L);
        return self.getClass().getName();
    }

    private static class CheckAnnotation {

        static {
            LogDelegate.log(TestInline.class.getSimpleName(), "checkAnnotation" + ": " + sCheckAnnotation);
        }

        private static void clinit() {
        }

        @Inline
        static void checkInline(char v0, String v1, int v2, float v3, String v4, int v5, long v6) {
            print(v4, v5, v5);
            print(v1, v2, v2);
        }

        private static void print(String a, int b, double c) {
            print(c);
            print(b);
            print(a);
        }

        private static void print(Object object) {
//        System.out.println(object);
        }
    }
}
