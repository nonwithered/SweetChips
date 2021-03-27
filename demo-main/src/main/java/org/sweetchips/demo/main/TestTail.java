package org.sweetchips.demo.main;

final class TestTail extends AbstractTest {

    private static long sMax;

    static void init(long max) {
        sMax = max;
    }

    @Override
    protected final void onTest() {
        new Thread(() -> {
            log("checkTail", recursive(0));
            try {
                log("checkOver", "begin");
                over(0);
            } catch (StackOverflowError e) {
                log("checkOver", e);
            } finally {
                log("checkOver", "end");
            }
        }).start();
    }

    long recursive(long x) {
        long y = x + 1;
        if (y > sMax) {
            return y;
        }
        return recursive(y);
    }

    long over(long x) {
        long y = x + 1;
        if (y > sMax) {
            return y;
        }
        return over(y);
    }
}

