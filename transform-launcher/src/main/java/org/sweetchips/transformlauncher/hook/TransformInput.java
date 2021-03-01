package org.sweetchips.transformlauncher.hook;

import java.util.Collection;

public interface TransformInput {

    Collection<JarInput> getJarInputs();

    Collection<DirectoryInput> getDirectoryInputs();
}
