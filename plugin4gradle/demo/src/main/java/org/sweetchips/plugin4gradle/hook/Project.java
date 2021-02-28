package org.sweetchips.plugin4gradle.hook;

public interface Project {

    PluginContainer getPlugins();

    ExtensionContainer getExtensions();
}
