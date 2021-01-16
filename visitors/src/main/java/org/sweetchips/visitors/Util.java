package org.sweetchips.visitors;

import org.objectweb.asm.ClassVisitor;
import org.sweetchips.base.Hide;
import org.sweetchips.base.Uncheckcast;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public interface Util {

    String HIDE_NAME = "L" + Hide.class.getName().replace(".", "/") + ";";
    Map<String, Collection<HideElement>> HIDE_TARGET = new ConcurrentHashMap<>();

    String UNCHECKCAST_NAME = "L" + Uncheckcast.class.getName().replace(".", "/") + ";";
    Map<String, Map<UncheckcastElement, UncheckcastElement>> UNCHECKCAST_TARGET = new ConcurrentHashMap<>();
    String VALUE_NAME = "value";

    @SafeVarargs
    static ClassVisitor newInstance(int api, ClassVisitor visitor, Class<? extends ClassVisitor>... clazzes) {
        AtomicReference<ClassVisitor> ref = new AtomicReference<>(visitor);
        Arrays.asList(clazzes).forEach((clazz) -> ref.set(newInstance(api, ref.get(), clazz)));
        return ref.get();
    }

    static ClassVisitor newInstance(int api, ClassVisitor cv, Class<? extends ClassVisitor> clazz) {
        try {
            Constructor<? extends ClassVisitor> constructor = clazz.getConstructor(int.class, ClassVisitor.class);
            constructor.setAccessible(true);
            return constructor.newInstance(api, cv);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
