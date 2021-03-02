package org.sweetchips.transformlauncher.bridge;

import java.util.Collection;

public interface TransformInput {

    Collection<JarInput> getJarInputs();

    Collection<DirectoryInput> getDirectoryInputs();
}
