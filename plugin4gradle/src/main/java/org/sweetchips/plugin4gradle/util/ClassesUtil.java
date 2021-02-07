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
            Constructor<? extends ClassVisitor> constructor = clazz.getConstructor(int.class, ClassVisitor.class);
            constructor.setAccessible(true);
            return constructor.newInstance(api, cv);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
