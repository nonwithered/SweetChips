package main;

import java.lang.reflect.Member;

import test.Test;

import org.sweetchips.annotation.Uncheckcast;

public class Main {

    @Uncheckcast({String.class})
    public static void main(String[] args) {
        Object test = new Test("default").test();
        try {
            String s = (String) test;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        System.out.println(checkFlags(0x00001000, Test.class, "test", String.class));
        System.out.println(checkFlags(0x00001000, Test.class, "<init>"));
    }

    private static boolean checkFlags(int flags, Class<?> clazz, String methodName, Class<?>... argsTypes) {
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