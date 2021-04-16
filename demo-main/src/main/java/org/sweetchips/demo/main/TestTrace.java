package org.sweetchips.demo.main;

final class TestTrace extends AbstractTest {

    private static final int sDepth = 40;

    private static final long sTime = 100;

    private int mDepth;

    @Override
    protected final void onTest() {
        new Thread(this::run).start();
    }

    void run() {
        if (++mDepth > sDepth) {
            return;
        }
        try {
            Thread.sleep(sTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        run();
    }
}
