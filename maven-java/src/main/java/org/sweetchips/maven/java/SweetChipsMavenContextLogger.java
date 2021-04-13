package org.sweetchips.maven.java;

import org.apache.maven.plugin.logging.Log;
import org.sweetchips.platform.common.ContextLogger;

final class SweetChipsMavenContextLogger extends ContextLogger {

    private final Log mLog;

    public SweetChipsMavenContextLogger(Log log) {
        mLog = log;
    }

    @Override
    public void d(String tag, String msg) {
        mLog.debug(tag + ": " + msg);
    }

    @Override
    public void d(String tag, Throwable e) {
        mLog.debug(tag, e);
    }

    @Override
    public void i(String tag, String msg) {
        mLog.info(tag + ": " + msg);
    }

    @Override
    public void i(String tag, Throwable e) {
        mLog.info(tag, e);
    }

    @Override
    public void w(String tag, String msg) {
        mLog.warn(tag + ": " + msg);
    }

    @Override
    public void w(String tag, Throwable e) {
        mLog.warn(tag, e);
    }

    @Override
    public void e(String tag, String msg) {
        mLog.error(tag + ": " + msg);
    }

    @Override
    public void e(String tag, Throwable e) {
        mLog.error(tag, e);
    }
}
