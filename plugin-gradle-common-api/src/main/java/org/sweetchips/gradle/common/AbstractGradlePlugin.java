package org.sweetchips.gradle.common;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.sweetchips.platform.jvm.WorkflowSettings;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractGradlePlugin<E extends AbstractExtension> implements Plugin<Project> {

    private Project mProject;
    private E mExtension;

    public final Project getProject() {
        return mProject;
    }

    public final E getExtension() {
        return mExtension;
    }

    public abstract String getName();

    protected abstract void onApply(Project project);

    @Override
    public final void apply(Project project) {
        init(project);
        onApply(project);
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
        mExtension = project.getExtensions().create(getName(), clazz, this);
    }

    protected final WorkflowSettings getWorkflowSettings(String name) {
        return (WorkflowSettings) getProject().getExtensions().findByName(name);
    }
}