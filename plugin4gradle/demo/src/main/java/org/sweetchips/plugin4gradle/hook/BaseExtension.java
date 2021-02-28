package org.sweetchips.plugin4gradle.hook;

public abstract class BaseExtension {

    public abstract void registerTransform(Transform transform, Object... dependencies);
}
