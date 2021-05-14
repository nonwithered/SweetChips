package org.sweetchips.platform.jvm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.utility.ClassesUtil;

import java.lang.reflect.Constructor;
import java.util.Map;

public interface ClassVisitorFactory {

    static ClassVisitorFactory fromClassVisitor(Class<? extends ClassVisitor> clazz) {
        try {
            Constructor<? extends ClassVisitor> constructor = ClassesUtil.getDeclaredConstructor(clazz, int.class, ClassVisitor.class, Map.class);
            return ((api, cv, ext) -> ClassesUtil.newInstance(constructor, api, cv, ext));
        } catch (Throwable e) {
            Constructor<? extends ClassVisitor> constructor = ClassesUtil.getDeclaredConstructor(clazz, int.class, ClassVisitor.class);
            return ((api, cv, ext) -> ClassesUtil.newInstance(constructor, api, cv));
        }
    }

    static ClassVisitorFactory fromClassNode(Class<? extends ClassNode> clazz) {
        ClassVisitorFactory f;
        try {
            Constructor<? extends ClassNode> constructor = ClassesUtil.getDeclaredConstructor(clazz, int.class, Map.class);
            f = ((api, cv, ext) -> ClassesUtil.newInstance(constructor, api, ext));
        } catch (Throwable e) {
            Constructor<? extends ClassNode> constructor = ClassesUtil.getDeclaredConstructor(clazz, int.class);
            f = ((api, cv, ext) -> ClassesUtil.newInstance(constructor, api));
        }
        ClassVisitorFactory factory = f;
        return (api, cv, ext) -> new ClassNodeAdaptor(api, cv, (ClassNode) factory.newInstance(api, null, ext));
    }

    ClassVisitor newInstance(int api, ClassVisitor cv, Map<Object, Object> ext);
}
