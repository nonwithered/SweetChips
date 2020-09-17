package org.sweetchips.visitors;

import org.sweetchips.base.Hide;
import org.sweetchips.base.Uncheckcast;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public interface Util {

    int ASM_API = Opcodes.ASM5;

    String HIDE_NAME = "L" + Hide.class.getName().replace(".", "/") + ";";
    Map<String, Collection<Elements>> HIDE_TARGET = new ConcurrentHashMap<>();

    String UNCHECKCAST_NAME = "L" + Uncheckcast.class.getName().replace(".", "/") + ";";
    Map<String, Collection<Elements>> UNCHECKCAST_TARGET = new ConcurrentHashMap<>();

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
