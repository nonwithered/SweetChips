package org.sweetchips.transformlauncher.bridge;

import java.io.IOException;
import java.util.Set;

public abstract class Transform {

    public abstract String getName();

    public abstract Set<QualifiedContent.ContentType> getInputTypes();

    public abstract Set<? super QualifiedContent.Scope> getScopes();

    public abstract boolean isIncremental();

    public abstract void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException;
}
