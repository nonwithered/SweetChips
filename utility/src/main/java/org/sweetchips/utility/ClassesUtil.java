package org.sweetchips.utility;

import java.lang.reflect.Constructor;

public interface ClassesUtil {

    @SuppressWarnings("unchecked")
    static <T> Class<T> forName(String name) {
        return AsyncUtil.call(() -> (Class<T>) Class.forName(name));
    }

    static <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Class<?>... args) {
        return AsyncUtil.call(() -> clazz.getDeclaredConstructor(args));
    }

    static <T> T newInstance(Constructor<T> constructor, Object... args) {
        return AsyncUtil.call(() ->  constructor.newInstance(args));
    }
}
