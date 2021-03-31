package org.sweetchips.platform.jvm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.ClassesUtil;

import java.lang.reflect.Constructor;
import java.util.Map;

public interface ClassVisitorFactory {

    static ClassVisitorFactory fromClassVisitor(Class<? extends ClassVisitor> clazz) {
        return AsyncUtil.call(() -> {
            try {
                Constructor<? extends ClassVisitor> constructor = ClassesUtil.getDeclaredConstructor(clazz, int.class, ClassVisitor.class, Map.class);
                return (api, cv, ext) -> AsyncUtil.call(() -> constructor.newInstance(api, cv, ext));
            } catch (Throwable e) {
                Constructor<? extends ClassVisitor> constructor = ClassesUtil.getDeclaredConstructor(clazz, int.class, ClassVisitor.class);
                return (api, cv, ext) -> AsyncUtil.call(() -> constructor.newInstance(api, cv));
            }
        });
    }

    static ClassVisitorFactory fromClassNode(Class<? extends ClassNode> clazz) {
        ClassVisitorFactory factory = AsyncUtil.call(() -> {
            try {
                Constructor<? extends ClassNode> constructor = ClassesUtil.getDeclaredConstructor(clazz, int.class, Map.class);
                return (api, cv, ext) -> AsyncUtil.call(() -> constructor.newInstance(api, ext));
            } catch (Throwable e) {
                Constructor<? extends ClassNode> constructor = ClassesUtil.getDeclaredConstructor(clazz, int.class);
                return (api, cv, ext) -> AsyncUtil.call(() -> constructor.newInstance(api));
            }
        });
        return (api, cv, ext) -> new ClassNodeAdaptor(api, cv, (ClassNode) factory.newInstance(api, null, ext));
    }

    ClassVisitor newInstance(int api, ClassVisitor cv, Map<Object, Object> ext);
}
