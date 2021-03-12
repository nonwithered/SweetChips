package org.sweetchips.demo.main;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

abstract class AbstractTest {

    @SafeVarargs
    static void testAll(Class<? extends AbstractTest>... classes) {
        for (Class<? extends AbstractTest> clazz : classes) {
            test(clazz);
        }
    }

    private static void test(Class<? extends AbstractTest> clazz) {
        try {
        Constructor<? extends AbstractTest> constructor = clazz.getDeclaredConstructor();
        AbstractTest test = constructor.newInstance();
        test.onTest();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected abstract void onTest();

    protected final void log(String tag, Object msg) {
        LogDelegate.log(mTag, tag + ": " + msg);
    }

    private final String mTag;

    protected AbstractTest() {
        mTag = getClassName();
    }

    private String getClassName() {
        return getClass().getSimpleName();
    }
}
