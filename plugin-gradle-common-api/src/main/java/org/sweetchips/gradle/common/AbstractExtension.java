package org.sweetchips.gradle.common;

import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.WorkflowSettings;
import org.sweetchips.utility.ClassesUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Function;

public abstract class AbstractExtension<C extends BasePluginContext> {

    private Function<String, WorkflowSettings> mSettings;
    private final C mContext;

    public AbstractExtension() {
        Type type = getClass();
        while (!(type instanceof ParameterizedType)) {
            type = ((Class<?>) type).getGenericSuperclass();
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        @SuppressWarnings("unchecked")
        Class<C> clazz = (Class<C>) parameterizedType.getActualTypeArguments()[0];
        mContext = ClassesUtil.newInstance(ClassesUtil.getDeclaredConstructor(clazz));
    }

    public final C getContext() {
        return mContext;
    }

    public final void attach(String name) {
        if (mSettings == null) {
            throw new IllegalStateException();
        }
        getContext().onAttach(mSettings.apply(name));
        mSettings = null;
    }

    public final void ignore(String... name) {
        Arrays.asList(name).forEach(getContext()::addIgnore);
    }

    public final void notice(String... name) {
        Arrays.asList(name).forEach(getContext()::addNotice);
    }

    final void setSettings(Function<String, WorkflowSettings> settings) {
        mSettings = settings;
    }
}
