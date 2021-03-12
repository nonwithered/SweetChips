package org.sweetchips.plugin.gradle;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.common.jvm.ClassNodeAdaptor;
import org.sweetchips.common.jvm.ClassVisitorFactory;
import org.sweetchips.common.jvm.JvmContext;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.ClassesUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.Map;

import groovy.lang.Closure;

public class WorkflowExtension {

    private final WeakReference<JvmContext> mContext;

    public WorkflowExtension(JvmContext context) {
        mContext = new WeakReference<>(context);
    }

    public <V> V prepare(Closure<V> closure) {
        closure.setDelegate(new PrepareExt());
        return closure.call();
    }

    public class PrepareExt {

        public void before(Runnable runnable) {
            JvmContext context = mContext.get();
            if (context == null) {
                return;
            }
            context.addPrepareBefore(runnable);
        }

        public void first(ClassVisitorFactory factory) {
            JvmContext context = mContext.get();
            if (context == null) {
                return;
            }
            context.addPrepareFirst(factory);
        }

        public void first(Class<? extends ClassVisitor> clazz) {
            first(fromClass(clazz));
        }

        public void first(String name) {
            first(ClassesUtil.forName(name));
        }

        public void last(ClassVisitorFactory factory) {
            JvmContext context = mContext.get();
            if (context == null) {
                return;
            }
            context.addPrepareLast(factory);
        }

        public void last(Class<? extends ClassVisitor> clazz) {
            last(fromClass(clazz));
        }

        public void last(String name) {
            last(ClassesUtil.forName(name));
        }

        public void after(Runnable runnable) {
            JvmContext context = mContext.get();
            if (context == null) {
                return;
            }
            context.addPrepareAfter(runnable);
        }
    }

    public <V> V transform(Closure<V> closure) {
        closure.setDelegate(new TransformExt());
        return closure.call();
    }

    public class TransformExt {

        public void before(Runnable runnable) {
            JvmContext context = mContext.get();
            if (context == null) {
                return;
            }
            context.addTransformBefore(runnable);
        }

        public void first(ClassVisitorFactory factory) {
            JvmContext context = mContext.get();
            if (context == null) {
                return;
            }
            context.addTransformFirst(factory);
        }

        public void first(Class<? extends ClassVisitor> clazz) {
            first(fromClass(clazz));
        }

        public void first(String name) {
            first(ClassesUtil.forName(name));
        }

        public void last(ClassVisitorFactory factory) {
            JvmContext context = mContext.get();
            if (context == null) {
                return;
            }
            context.addTransformLast(factory);
        }

        public void last(Class<? extends ClassVisitor> clazz) {
            last(fromClass(clazz));
        }

        public void last(String name) {
            last(ClassesUtil.forName(name));
        }

        public void after(Runnable runnable) {
            JvmContext context = mContext.get();
            if (context == null) {
                return;
            }
            context.addTransformAfter(runnable);
        }
    }

    public ClassVisitorFactory adapt(Class<? extends ClassNode> clazz) {
        return adaptClass(clazz);
    }

    public ClassVisitorFactory adapt(String name) {
        return adapt(ClassesUtil.forName(name));
    }

    void setExtra(Map<?, ?> extra) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.setExtra(extra);
    }

    Map<?, ?> getExtra() {
        JvmContext context = mContext.get();
        if (context == null) {
            return null;
        }
        return context.getExtra();
    }

    private ClassVisitorFactory fromClass(Class<? extends ClassVisitor> clazz) {
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

    private ClassVisitorFactory adaptClass(Class<? extends ClassNode> clazz) {
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
}
