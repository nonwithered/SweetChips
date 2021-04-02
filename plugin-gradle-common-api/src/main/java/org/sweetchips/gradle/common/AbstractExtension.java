package org.sweetchips.gradle.common;

import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.utility.ClassesUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractExtension<C extends BasePluginContext> {

    private Consumer<String> mAttach;
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
        if (mAttach == null) {
            throw new IllegalStateException();
        }
        mAttach.accept(name);
        mAttach = null;
    }

    public final void ignore(String... name) {
        Arrays.asList(name).forEach(getContext()::addIgnore);
    }

    public final void notice(String... name) {
        Arrays.asList(name).forEach(getContext()::addNotice);
    }

    final void setAttach(Consumer<String> attach) {
        mAttach = attach;
    }
}
