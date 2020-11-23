package org.sweetchips.plugin4gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.objectweb.asm.ClassVisitor;

import java.util.Collection;
import java.util.Collections;

public abstract class BasePlugin implements Plugin<Project> {

    @Override
    public final void apply(Project project) {
        onApply(project);
        UnionContext.PREPARE.addAll(onPrepare());
        UnionContext.DUMP.addAll(onTransform());
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
}
