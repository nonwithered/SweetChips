package org.sweetchips.plugin4gradle;

import org.gradle.api.Project;
import org.objectweb.asm.ClassVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

final class UnionContext {

    private static final Map<String, UnionContext> sContexts = new HashMap<>();

    public static UnionContext getInstance(String name) {
        if (name == null) {
            name = Util.NAME;
        }
        UnionContext context = sContexts.get(name);
        if (context == null) {
            context = new UnionContext(name);
            sContexts.put(name, context);
        }
        return context;
    }

    private static Project sProject;

    static void setProject(Project project) {
        sProject = project;
    }

    static Project getProject() {
        return sProject;
    }

    private static UnionPlugin sPlugin;

    static void setPlugin(UnionPlugin plugin) {
        sPlugin = plugin;
    }

    static UnionPlugin getPlugin() {
        return sPlugin;
    }

    private static UnionExtension sExtension;

    static void setExtension(UnionExtension extension) {
        sExtension = extension;
    }

    static UnionExtension getExtension() {
        return sExtension;
    }

    public static void addPrepare(String name, Collection<Class<? extends ClassVisitor>> visitors) {
        if (name == null) {
            name = Util.NAME;
        }
        getInstance(name).mPrepare.addAll(visitors);
    }

    public static void addTransform(String name, Collection<Class<? extends ClassVisitor>> visitors) {
        if (name == null) {
            name = Util.NAME;
        }
        getInstance(name).mTransform.addAll(visitors);
    }

    private final String mName;

    private final Collection<Class<? extends ClassVisitor>> mPrepare = new ArrayList<>();

    private final Collection<Class<? extends ClassVisitor>> mTransform = new ArrayList<>();

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
}
