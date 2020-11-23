package org.sweetchips.visitors;

import org.sweetchips.base.Hide;
import org.sweetchips.base.Uncheckcast;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public interface Util {

    AtomicInteger ASM_API = new AtomicInteger(Opcodes.ASM5);

    String HIDE_NAME = "L" + Hide.class.getName().replace(".", "/") + ";";
    Map<String, Collection<HideElement>> HIDE_TARGET = new ConcurrentHashMap<>();

    String UNCHECKCAST_NAME = "L" + Uncheckcast.class.getName().replace(".", "/") + ";";
    Map<String, Map<UncheckcastElement, UncheckcastElement>> UNCHECKCAST_TARGET = new ConcurrentHashMap<>();
    String VALUE_NAME = "value";

    @SafeVarargs
    static ClassVisitor newInstance(ClassVisitor cv, Function<ClassVisitor, ClassVisitor>... functions) {
        for (Function<ClassVisitor, ClassVisitor> function : functions) {
            cv = function.apply(cv);
        }
        return cv;
    }

    @SafeVarargs
    static ClassVisitor newInstance(int api, Function<ClassVisitor, ClassVisitor>... functions) {
        ClassVisitor cv = new ClassVisitor(api) {};
        for (Function<ClassVisitor, ClassVisitor> function : functions) {
            cv = function.apply(cv);
        }
        return cv;
    }
}
