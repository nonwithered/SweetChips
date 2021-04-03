package org.sweetchips.gradle.common;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.WorkflowSettings;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
        Type type = getClass();
        while (!(type instanceof ParameterizedType)) {
            type = ((Class<?>) type).getGenericSuperclass();
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        @SuppressWarnings("unchecked")
        Class<E> clazz = (Class<E>) parameterizedType.getActualTypeArguments()[0];
        mExtension = project.getExtensions().create(getName(), clazz);
        mExtension.setSettings(this::getWorkflowSettings);
    }

    private final WorkflowSettings getWorkflowSettings(String name) {
        return (WorkflowSettings) getProject().getExtensions().findByName(name);
    }
}