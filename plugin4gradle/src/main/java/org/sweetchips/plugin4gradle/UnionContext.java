package org.sweetchips.plugin4gradle;

import org.gradle.api.Project;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class UnionContext {

    private static final Map<String, UnionContext> sContexts = new HashMap<>();

    public static UnionContext getInstance(String task) {
        if (task == null) {
            task = Util.NAME;
        }
        UnionContext context = sContexts.get(task);
        if (context == null) {
            context = new UnionContext(task);
            sContexts.put(task, context);
        }
        return context;
    }

    private static UnionExtension sExtension;

    private static Project sProject;

    private static UnionPlugin sPlugin;

    static void setExtension(UnionExtension extension) {
        sExtension = extension;
    }

    static UnionExtension getExtension() {
        return sExtension;
    }

    static void setProject(Project project) {
        sProject = project;
    }

    static Project getProject() {
        return sProject;
    }

    static void setPlugin(UnionPlugin plugin) {
        sPlugin = plugin;
    }

    static UnionPlugin getPlugin() {
        return sPlugin;
    }

    static void addClassVisitor(AbstractPlugin.ActionType type, AbstractPlugin.ActionMode mode, String task, Class<? extends ClassVisitor> visitor) {
        if (task == null) {
            task = Util.NAME;
        }
        UnionContext context = getInstance(task);
        Deque<Class<? extends ClassVisitor>> deque = null;
        switch (type) {
            case PREPARE:
                deque = context.mPrepare;
                break;
            case TRANSFORM:
                deque = context.mTransform;
                break;
        }
        switch (mode) {
            case FIRST:
                deque.offerFirst(visitor);
                break;
            case LAST:
                deque.offerLast(visitor);
                break;
        }
    }

    static void createClass(String task, String name, ClassNode cn) {
        if (task == null) {
            task = Util.NAME;
        }
        getInstance(task).mClassNode.put(name, cn);
    }

    private final String mName;

    private final Deque<Class<? extends ClassVisitor>> mPrepare = new LinkedList<>();

    private final Deque<Class<? extends ClassVisitor>> mTransform = new LinkedList<>();

    private final Map<String, ClassNode> mClassNode = new HashMap<>();

    private UnionContext(String name) {
        mName = name;
    }

    String getName() {
        return mName;
    }

    void forEachPrepare(Consumer<Class<? extends ClassVisitor>> consumer) {
        mPrepare.forEach(consumer);
    }

    void forEachTransform(Consumer<Class<? extends ClassVisitor>> consumer) {
        mTransform.forEach(consumer);
    }

    void forEachCreateClass(BiConsumer<String, ClassNode> consumer) {
        mClassNode.forEach(consumer);
    }
}
