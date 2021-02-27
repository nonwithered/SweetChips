package org.sweetchips.plugin4gradle.util;

import org.objectweb.asm.ClassVisitor;

import java.lang.reflect.Constructor;

public interface ClassesUtil {

    @SuppressWarnings("unchecked")
    static Class<? extends ClassVisitor> forName(String name) {
        return AsyncUtil.call(() -> (Class<? extends ClassVisitor>) Class.forName(name)).get();
    }

    static <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Class<?>... args) {
        return AsyncUtil.call(() -> clazz.getDeclaredConstructor(args)).get();
    }

    static <T> T newInstance(Constructor<T> constructor, Object... args) {
        return AsyncUtil.call(() ->  constructor.newInstance(args)).get();
    }

    static ClassVisitor newClassVisitor(int api, ClassVisitor cv, Class<? extends ClassVisitor> clazz) {
        return newInstance(getDeclaredConstructor(clazz, int.class, ClassVisitor.class), api, cv);
    }

    static void checkAssert(boolean b) {
        if (!b) {
            throw new AssertionError();
        }
    }

    static boolean checkAccess(int access, int flag) {
        return (access & flag) != 0;
    }
}
