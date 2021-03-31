package org.sweetchips.gradle.common;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.common.jvm.ClassVisitorFactory;
import org.sweetchips.common.jvm.JvmContext;
import org.sweetchips.gradle.WorkflowSettings;
import org.sweetchips.utility.ClassesUtil;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import groovy.lang.Closure;

public class WorkflowExtension implements WorkflowSettings {

    final WeakReference<JvmContext> mContext;

    public WorkflowExtension(JvmContext context) {
        mContext = new WeakReference<>(context);
    }

    public <V> V prepare(Closure<V> closure) {
        closure.setDelegate(new PrepareExt());
        return closure.call();
    }

    public class PrepareExt {

        public void before(Consumer<Map<Object, Object>> consumer) {
            addPrepareBefore(consumer);
        }

        public void first(ClassVisitorFactory factory) {
            addPrepareFirst(factory);
        }

        public void first(Class<? extends ClassVisitor> clazz) {
            first(ClassVisitorFactory.fromClassVisitor(clazz));
        }

        public void first(String name) {
            first(ClassesUtil.forName(name));
        }

        public void last(ClassVisitorFactory factory) {
            addPrepareLast(factory);
        }

        public void last(Class<? extends ClassVisitor> clazz) {
            last(ClassVisitorFactory.fromClassVisitor(clazz));
        }

        public void last(String name) {
            last(ClassesUtil.forName(name));
        }

        public void after(Consumer<Map<Object, Object>> consumer) {
            addPrepareAfter(consumer);
        }
    }

    public <V> V transform(Closure<V> closure) {
        closure.setDelegate(new TransformExt());
        return closure.call();
    }

    public class TransformExt {

        public void before(Consumer<Map<Object, Object>> consumer) {
            addTransformBefore(consumer);
        }

        public void first(ClassVisitorFactory factory) {
            addTransformFirst(factory);
        }

        public void first(Class<? extends ClassVisitor> clazz) {
            first(ClassVisitorFactory.fromClassVisitor(clazz));
        }

        public void first(String name) {
            first(ClassesUtil.forName(name));
        }

        public void last(ClassVisitorFactory factory) {
            addTransformLast(factory);
        }

        public void last(Class<? extends ClassVisitor> clazz) {
            last(ClassVisitorFactory.fromClassVisitor(clazz));
        }

        public void last(String name) {
            last(ClassesUtil.forName(name));
        }

        public void after(Consumer<Map<Object, Object>> consumer) {
            addTransformAfter(consumer);
        }
    }

    public ClassVisitorFactory adapt(Class<? extends ClassNode> clazz) {
        return ClassVisitorFactory.fromClassNode(clazz);
    }

    public ClassVisitorFactory adapt(String name) {
        return adapt(ClassesUtil.forName(name));
    }

    void setExtra(Map<Object, Object> extra) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.setExtra(extra);
    }

    Map<Object, Object> getExtra() {
        JvmContext context = mContext.get();
        if (context == null) {
            return null;
        }
        return context.getExtra();
    }

    @Override
    public void addPrepareFirst(ClassVisitorFactory factory) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.addPrepareFirst(factory);
    }

    @Override
    public void addPrepareLast(ClassVisitorFactory factory) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.addPrepareLast(factory);
    }

    @Override
    public void addPrepareBefore(Consumer<Map<Object, Object>> consumer) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.addPrepareBefore(consumer);
    }

    @Override
    public void addPrepareAfter(Consumer<Map<Object, Object>> consumer) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.addPrepareAfter(consumer);
    }

    @Override
    public void addTransformFirst(ClassVisitorFactory factory) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.addTransformFirst(factory);
    }

    @Override
    public void addTransformLast(ClassVisitorFactory factory) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.addTransformLast(factory);
    }

    @Override
    public void addTransformBefore(Consumer<Map<Object, Object>> consumer) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.addTransformBefore(consumer);
    }

    @Override
    public void addTransformAfter(Consumer<Map<Object, Object>> consumer) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.addTransformAfter(consumer);
    }

    @Override
    public void addClass(Supplier<ClassNode> cn) {
        JvmContext context = mContext.get();
        if (context == null) {
            return;
        }
        context.addClass(cn);
    }

    @Override
    public int getAsmApi() {
        JvmContext context = mContext.get();
        if (context == null) {
            return -1;
        }
        return context.getApi();
    }
}
