package org.sweetchips.demo.main;

final class TestTail extends AbstractTest {

    private static final long sMax = Short.MAX_VALUE;

    @Override
    protected final void onTest() {
        new Thread(() -> {
            try {
                log("checkTail", recursive(0) > sMax);
            } catch (StackOverflowError e) {
                log("checkTail", false);
            }
            boolean ex = false;
            try {
                over(0);
            } catch (StackOverflowError e) {
                ex = true;
            } finally {
                log("checkOver", ex);
            }
        }).start();
    }

    private long recursive(long x) {
        long y = x + 1;
        if (y > sMax) {
            return y;
        }
        return recursive(y);
    }

    private void over(long x) {
        long y = x + 1;
        if (y > sMax) {
            return;
        }
        over(y);
    }
}

