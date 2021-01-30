package org.sweetchips.test;

import org.sweetchips.annotation.Hide;

import org.sweetchips.annotation.Uncheckcast;

public class Test extends Main {

    private static final String TAG = "Test";

    @Hide
    public Test() {
        this("default");
    }

    public Test(String msg) {
        TestLogger.log(TAG, "init: " + msg);
    }

    @Uncheckcast({Test.class, Main.class})
    public Test test() {
        Test test = new Test();
        Object object = test;
        Main main = (Main) object;
        ((Test) main).test("default");
        return (Test) main;
    }

    @Hide
    public Test test(String msg) {
        TestLogger.log(TAG, "test: " + msg);
        return this;
    }
}