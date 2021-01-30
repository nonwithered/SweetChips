package org.sweetchips.test;

public final class TestLogger {

    private static final String TAG = "Log";

    public interface ILogger {

        void log(String tag, String msg);
    }

    public static void log(String tag, Object msg) {
        sLogger.log(tag, String.valueOf(msg));
    }

    public static void log(ILogger logger) {
        sLogger = logger;
    }

    private static ILogger sLogger = (tag, msg) -> System.err.println(tag + ": " + msg);

    private TestLogger() {
        throw new UnsupportedOperationException();
    }
}
