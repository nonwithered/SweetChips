package org.sweetchips.transformlauncher.bridge;

public interface ExtensionContainer {

    <T> T create(String name, Class<T> clazz, Object... args);

    Object getByName(String name);
}
