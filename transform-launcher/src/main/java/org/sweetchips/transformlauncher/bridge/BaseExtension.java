package org.sweetchips.transformlauncher.bridge;

public abstract class BaseExtension {

    public abstract void registerTransform(Transform transform, Object... dependencies);
}
