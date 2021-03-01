package org.sweetchips.transformlauncher.hook;

public abstract class BaseExtension {

    public abstract void registerTransform(Transform transform, Object... dependencies);
}
