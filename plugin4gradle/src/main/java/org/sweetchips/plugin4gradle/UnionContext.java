package org.sweetchips.plugin4gradle;

import org.gradle.api.Project;
import org.objectweb.asm.ClassVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class UnionContext {

    private static final Map<String, UnionContext> sContexts = new HashMap<>();

    private static UnionContext sUnionContext;

    public static UnionContext newInstance(Project project, String name, UnionExtension extension) {
        if (name == null) {
            name = Util.NAME;
        }
        UnionContext context = sContexts.get(name);
        if (context == null) {
            context = new UnionContext(project, name, extension);
            sContexts.put(name, context);
            if (name.equals(Util.NAME)) {
                sUnionContext = context;
            }
        }
        return context;
    }

    public static void addPrepare(String name, Collection<Class<? extends ClassVisitor>> visitors) {
        UnionContext context = name == null ? sUnionContext : sContexts.get(name);
        if (context != null) {
            context.mPrepare.addAll(visitors);
        }
    }

    public static void addTransform(String name, Collection<Class<? extends ClassVisitor>> visitors) {
        UnionContext context = name == null ? sUnionContext : sContexts.get(name);
        if (context != null) {
            context.mTransform.addAll(visitors);
        }
    }

    private final Project mProject;

    private final String mName;

    private final UnionExtension mExt;

    private final Collection<Class<? extends ClassVisitor>> mPrepare = new ArrayList<>();

    private final Collection<Class<? extends ClassVisitor>> mTransform = new ArrayList<>();

    private UnionContext(Project project, String name, UnionExtension ext) {
        mProject = project;
        mName = name;
        mExt = ext;
    }

    Project getProject() {
        return mProject;
    }

    String getName() {
        return mName;
    }

    UnionExtension getExtension() {
        return mExt;
    }

    void forEachPrepare(Consumer<Class<? extends ClassVisitor>> consumer) {
        mPrepare.forEach(consumer);
    }

    void forEachTransform(Consumer<Class<? extends ClassVisitor>> consumer) {
        mTransform.forEach(consumer);
    }
}
