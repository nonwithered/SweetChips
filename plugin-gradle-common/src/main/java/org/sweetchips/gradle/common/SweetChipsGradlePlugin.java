package org.sweetchips.gradle.common;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.sweetchips.platform.jvm.JvmContext;

public abstract class SweetChipsGradlePlugin implements Plugin<Project> {

    private Project mProject;
    private SweetChipsExtension mExtension;

    @Override
    public final void apply(Project project) {
        mProject = project;
        mExtension = project.getExtensions().create(getName(), SweetChipsExtension.class, this);
        onApply();
    }

    public final String getName() {
        return "SweetChips";
    }

    public final SweetChipsExtension getExtension() {
        return mExtension;
    }

    public final Project getProject() {
        return mProject;
    }

    protected abstract void onApply();

    public abstract void registerTransform(String name, JvmContext context);
}
