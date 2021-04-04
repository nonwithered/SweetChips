package org.sweetchips.platform.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

public class ContextLogger {

    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;

    private static final ContextLogger sDefault = new ContextLogger();

    public static ContextLogger getDefault() {
        return sDefault;
    }

    public void d(String tag, String msg) {
        System.out.println(String.format(Locale.getDefault(), "I/%s: %s", tag, msg));
    }

    public void d(String tag, Throwable e) {
        System.out.println(String.format(Locale.getDefault(), "I/%s: %s", tag, printStackTrace(e)));
    }

    public void i(String tag, String msg) {
        System.out.println(String.format(Locale.getDefault(), "D/%s: %s", tag, msg));
    }

    public void i(String tag, Throwable e) {
        System.out.println(String.format(Locale.getDefault(), "D/%s: %s", tag, printStackTrace(e)));
    }

    public void w(String tag, String msg) {
        System.err.println(String.format(Locale.getDefault(), "W/%s: %s", tag, msg));
    }

    public void w(String tag, Throwable e) {
        System.err.println(String.format(Locale.getDefault(), "W/%s: %s", tag, printStackTrace(e)));
    }

    public void e(String tag, String msg) {
        System.err.println(String.format(Locale.getDefault(), "E/%s: %s", tag, msg));
    }

    public void e(String tag, Throwable e) {
        System.err.println(String.format(Locale.getDefault(), "E/%s: %s", tag, printStackTrace(e)));
    }

    private static String printStackTrace(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
