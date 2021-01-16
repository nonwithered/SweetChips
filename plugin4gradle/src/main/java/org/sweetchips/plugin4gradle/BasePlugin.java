package org.sweetchips.plugin4gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.objectweb.asm.ClassVisitor;

import java.util.Arrays;
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

    @SafeVarargs
    protected final void addPrepare(String name, Class<? extends ClassVisitor>... cv) {
        UnionContext.addPrepare(name, Arrays.asList(cv));
    }

    @SafeVarargs
    protected final void addTransform(String name, Class<? extends ClassVisitor>... cv) {
        UnionContext.addTransform(name, Arrays.asList(cv));
    }

    private void init(Project project) {
        if (project.getPlugins().findPlugin(Util.NAME) == null) {
            throw new ProjectConfigurationException(Util.NAME + " plugin should be enabled first",
                    new IllegalStateException(Util.NAME + " plugin should be enabled first"));
        }
    }

    private void prepare() {
        UnionContext.addPrepare(null, onPrepare());
    }

    private void transform() {
        UnionContext.addTransform(null, onPrepare());
    }
}
