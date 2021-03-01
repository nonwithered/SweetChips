package org.sweetchips.transformlauncher.hook;

public interface ExtensionContainer {

    <T> T create(String name, Class<T> clazz, Object... args);

    Object getByName(String name);
}
