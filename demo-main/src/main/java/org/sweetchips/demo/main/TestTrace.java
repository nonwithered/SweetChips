package org.sweetchips.demo.main;

final class TestTrace extends AbstractTest {

    private static int sDepth;

    private static long sTime;

    private static Runnable sCallback = () -> LogDelegate.log("(depth, time)", "(" + sDepth + ", " + sTime + ")");

    static void init(int depth, long time, Runnable callback) {
        sDepth = depth;
        sTime = time;
        sCallback = callback;
    }

    private int mDepth;

    @Override
    protected final void onTest() {
        new Thread(this::run).start();
    }

    void run() {
        if (++mDepth > sDepth) {
            sCallback.run();
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
