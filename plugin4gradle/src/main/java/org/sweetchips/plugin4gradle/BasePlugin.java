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
        ensure(project);
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

    protected void addPrepare(String name, Class<? extends ClassVisitor> cv) {
        BaseContext context = Util.CONTEXTS.get(name);
        if (context != null) {
            context.addPrepare(cv);
        }
    }

    protected void addTransform(String name, Class<? extends ClassVisitor> cv) {
        BaseContext context = Util.CONTEXTS.get(name);
        if (context != null) {
            context.addDump(cv);
        }
    }

    private void prepare() {
        BaseContext context = Util.CONTEXTS.get(Util.NAME);
        onPrepare().forEach(context::addPrepare);
    }

    private void transform() {
        BaseContext context = Util.CONTEXTS.get(Util.NAME);
        onTransform().forEach(context::addDump);
    }

    private void ensure(Project project) {
        if (project.getPlugins().findPlugin("com.android.application") == null
                && project.getPlugins().findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("SweetChips plugin should be enabled first", (Throwable) null);
        }
    }
}
