package org.sweetchips.plugin4gradle.hook;

import java.util.Collection;

public interface TransformInput {

    Collection<JarInput> getJarInputs();

    Collection<DirectoryInput> getDirectoryInputs();
}
