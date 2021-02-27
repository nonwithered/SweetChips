package org.sweetchips.plugin4gradle.util;

import org.objectweb.asm.ClassVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public interface ClassesUtil {

    @SuppressWarnings("unchecked")
    static Class<? extends ClassVisitor> forName(String name) {
        try {
            return (Class<? extends ClassVisitor>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static ClassVisitor newInstance(int api, ClassVisitor cv, Class<? extends ClassVisitor> clazz) {
        try {
            Constructor<? extends ClassVisitor> constructor = clazz.getDeclaredConstructor(int.class, ClassVisitor.class);
            return constructor.newInstance(api, cv);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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
