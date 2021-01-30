package org.sweetchips.plugin4gradle;

import com.android.annotations.NonNull;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.objectweb.asm.ClassVisitor;

import java.util.Collection;

public abstract class BasePlugin implements Plugin<Project> {

    @Override
    public final void apply(@NonNull Project project) {
        throw new RuntimeException("Stub!");
    }

    protected abstract void onApply(Project project);

    protected Collection<Class<? extends ClassVisitor>> onPrepare() {
        throw new RuntimeException("Stub!");
    }

    protected Collection<Class<? extends ClassVisitor>> onTransform() {
        throw new RuntimeException("Stub!");
    }

    @SafeVarargs
    protected final void addPrepare(String name, Class<? extends ClassVisitor>... cv) {
        throw new RuntimeException("Stub!");
    }

    @SafeVarargs
    protected final void addTransform(String name, Class<? extends ClassVisitor>... cv) {
        throw new RuntimeException("Stub!");
    }
}
