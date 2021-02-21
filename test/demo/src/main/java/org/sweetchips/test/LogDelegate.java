package org.sweetchips.test;

import org.sweetchips.annotation.Hide;

@Hide
final class LogDelegate {

    private static Main.ILog sLog = (tag, msg) -> System.err.println(tag + ": " + msg);

    private LogDelegate() {
        throw new UnsupportedOperationException();
    }

    static void log(String tag, Object msg) {
        sLog.log(tag, String.valueOf(msg));
    }

    static void log(Main.ILog log) {
        sLog = log;
    }
}
