package org.sweetchips.plugin.gradle;

import com.android.annotations.NonNull;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractPlugin<E extends AbstractExtension> implements Plugin<Project> {

    private Project mProject;
    private E mExtension;

    public final Project getProject() {
        return mProject;
    }

    public final E getExtension() {
        return mExtension;
    }

    protected abstract String getName();

    protected abstract void onApply(Project project);

    @Override
    public final void apply(@NonNull Project project) {
        init(project);
        onApply(project);
    }

    protected String getStaticFieldName() {
        return "INSTANCE";
    }

    @SuppressWarnings("unchecked")
    private void init(Project project) {
        mProject = project;
        Type type = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Class<E> clazz = (Class<E>) parameterizedType.getActualTypeArguments()[0];
        mExtension = project.getExtensions().create(getName(), clazz);
        try {
            Field field = getClass().getDeclaredField(getStaticFieldName());
            field.setAccessible(true);
            field.set(null, this);
        } catch (Exception e) {
            // ignore
        }
    }
}