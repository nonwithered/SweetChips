package org.sweetchips.test;

import java.lang.reflect.Member;

import org.sweetchips.annotation.Uncheckcast;

public class Main {

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
            TestLogger.log(TAG, e.toString());
        }
        TestLogger.log(TAG, checkFlags(0x00001000, Test.class, "test", String.class));
        TestLogger.log(TAG, checkFlags(0x00001000, Test.class, "<init>"));
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
            throw new IllegalArgumentException(e);
        }
        return (member.getModifiers() & flags) == flags;
    }
}