package org.sweetchips.plugin4gradle;

import com.android.annotations.NonNull;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.objectweb.asm.ClassVisitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public abstract class BasePlugin implements Plugin<Project> {

    @Override
    public final void apply(@NonNull Project project) {
        init(project);
        onApply(project);
    }

    protected abstract void onApply(Project project);

    @SafeVarargs
    protected final void addFirstPrepare(String name, Class<? extends ClassVisitor>... cv) {
        UnionContext.addFirstPrepare(name, Arrays.asList(cv));
    }

    @SafeVarargs
    protected final void addLastPrepare(String name, Class<? extends ClassVisitor>... cv) {
        UnionContext.addLastPrepare(name, Arrays.asList(cv));
    }

    @SafeVarargs
    protected final void addFirstTransform(String name, Class<? extends ClassVisitor>... cv) {
        UnionContext.addFirstTransform(name, Arrays.asList(cv));
    }

    @SafeVarargs
    protected final void addLastTransform(String name, Class<? extends ClassVisitor>... cv) {
        UnionContext.addLastTransform(name, Arrays.asList(cv));
    }

    private void init(Project project) {
        if (UnionContext.getProject() == null) {
            throw new ProjectConfigurationException(Util.NAME + " plugin should be enabled first",
                    new RuntimeException(Util.NAME + " plugin should be enabled first"));
        }
    }
}
