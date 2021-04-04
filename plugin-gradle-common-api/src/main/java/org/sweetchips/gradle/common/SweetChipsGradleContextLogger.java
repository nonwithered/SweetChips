package org.sweetchips.gradle.common;

import org.gradle.api.logging.Logger;
import org.sweetchips.platform.common.ContextLogger;

final class SweetChipsGradleContextLogger extends ContextLogger {

    private final Logger mLogger;

    public SweetChipsGradleContextLogger(Logger logger) {
        mLogger = logger;
    }

    @Override
    public void d(String tag, String msg) {
        mLogger.debug(tag + ": " + msg);
    }

    @Override
    public void d(String tag, Throwable e) {
        mLogger.debug(tag, e);
    }

    @Override
    public void i(String tag, String msg) {
        mLogger.info(tag + ": " + msg);
    }

    @Override
    public void i(String tag, Throwable e) {
        mLogger.info(tag, e);
    }

    @Override
    public void w(String tag, String msg) {
        mLogger.warn(tag + ": " + msg);
    }

    @Override
    public void w(String tag, Throwable e) {
        mLogger.warn(tag, e);
    }

    @Override
    public void e(String tag, String msg) {
        mLogger.error(tag + ": " + msg);
    }

    @Override
    public void e(String tag, Throwable e) {
        mLogger.error(tag, e);
    }
}
