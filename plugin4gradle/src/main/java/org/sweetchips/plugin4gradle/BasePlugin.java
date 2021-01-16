package org.sweetchips.plugin4gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.objectweb.asm.ClassVisitor;

import java.util.Collection;
import java.util.Collections;

public abstract class BasePlugin implements Plugin<Project> {

    @Override
    public final void apply(Project project) {
        init(project);
        onApply(project);
        prepare();
        transform();
    }

    protected abstract void onApply(Project project);

    @SuppressWarnings("unchecked")
    protected Collection<Class<? extends ClassVisitor>> onPrepare() {
        return (Collection<Class<? extends ClassVisitor>>) Collections.EMPTY_LIST;
    }

    @SuppressWarnings("unchecked")
    protected Collection<Class<? extends ClassVisitor>> onTransform() {
        return (Collection<Class<? extends ClassVisitor>>) Collections.EMPTY_LIST;
    }

    protected final void addPrepare(String name, Class<? extends ClassVisitor> cv) {
        UnionContext context = Util.CONTEXTS.get(name);
        if (context != null) {
            context.addPrepare(cv);
        }
    }

    protected final void addTransform(String name, Class<? extends ClassVisitor> cv) {
        UnionContext context = Util.CONTEXTS.get(name);
        if (context != null) {
            context.addDump(cv);
        }
    }

    private void prepare() {
        UnionContext context = Util.CONTEXTS.get(Util.NAME);
        if (context != null) {
            onPrepare().forEach(context::addPrepare);
        }
    }

    private void transform() {
        UnionContext context = Util.CONTEXTS.get(Util.NAME);
        if (context != null) {
            onTransform().forEach(context::addDump);
        }
    }

    private void init(Project project) {
        if (project.getPlugins().findPlugin(Util.NAME) == null) {
            throw new ProjectConfigurationException(Util.NAME + " plugin should be enabled first",
                    new IllegalStateException(Util.NAME + " plugin should be enabled first"));
        }
    }
}
