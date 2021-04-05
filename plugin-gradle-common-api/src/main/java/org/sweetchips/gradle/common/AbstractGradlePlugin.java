package org.sweetchips.gradle.common;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.WorkflowSettings;
import org.sweetchips.utility.ClassesUtil;

public abstract class AbstractGradlePlugin<E extends AbstractExtension<? extends BasePluginContext>> implements Plugin<Project> {

    private Project mProject;
    private E mExtension;

    public final Project getProject() {
        return mProject;
    }

    public final E getExtension() {
        return mExtension;
    }

    protected abstract String getName();

    protected void onApply() {
    }

    @Override
    public final void apply(Project project) {
        init(project);
        onApply();
    }

    private void init(Project project) {
        mProject = project;
        @SuppressWarnings("unchecked")
        Class<E> clazz = (Class<E>) ClassesUtil.getSuperTypeArgs(getClass(), AbstractGradlePlugin.class)[0];
        E extension = project.getExtensions().create(getName(), clazz);
        mExtension = extension;
        extension.getContext().setLogger(new SweetChipsGradleContextLogger(project.getLogger()));
        extension.setSettings(this::getWorkflowSettings);
    }

    private WorkflowSettings getWorkflowSettings(String name) {
        return (WorkflowSettings) getProject().getExtensions().findByName(name);
    }
}