package org.sweetchips.demo.main;

public class Main {

    public static void main(String[] args) {
        AbstractTest.testAll(
                TestHide.class,
                TestUncheckcast.class,
                TestConst.class,
                TestTrace.class,
                TestInline.class,
                TestTail.class
        );
    }

    public static void setLog(ILog log) {
        LogDelegate.log(log);
    }

    public interface ILog {

        void log(String tag, String msg);
    }
}