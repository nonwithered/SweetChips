package org.sweetchips.test;

import java.lang.reflect.Member;
import java.util.Arrays;

import org.sweetchips.annotation.Uncheckcast;

public class Main {

    private static final double sConstant = 123.456;

    private static final String TAG = "Test";

    public static void main(String[] args) {
        new Main().test(args);
    }

    @Uncheckcast({String.class})
    public void test(String[] args) {
        Object test = new Test("default").test();
        try {
            String s = (String) test;
        } catch (ClassCastException e) {
            TestLogger.log(TAG, e);
        }
        TestLogger.log(TAG, checkFlags(0x00001000, Test.class, "test", String.class));
        TestLogger.log(TAG, checkFlags(0x00001000, Test.class, "<init>"));
        TestLogger.log(TAG, Main.sConstant + " - " +
                Constant.INT + " - " +
                Constant.STR + " - " +
                Temp.NUM + " - " +
                Temp.STR);
        logFields("org.sweetchips.test.Constant");
        logFields("org.sweetchips.test.Temp");
        logFields("org.sweetchips.test.Main");
    }

    private boolean checkFlags(int flags, Class<?> clazz, String methodName, Class<?>... argsTypes) {
        Member member;
        try {
            if ("<init>".equals(methodName)) {
                member = clazz.getDeclaredConstructor(argsTypes);

            } else {
                member = clazz.getDeclaredMethod(methodName, argsTypes);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return (member.getModifiers() & flags) == flags;
    }

    private void logFields(String clazz) {
        try {
            TestLogger.log(TAG, clazz + ": " + Arrays.toString(Class.forName(clazz).getDeclaredFields()));
        } catch (Throwable e) {
            TestLogger.log(TAG, e);
        }
    }
}