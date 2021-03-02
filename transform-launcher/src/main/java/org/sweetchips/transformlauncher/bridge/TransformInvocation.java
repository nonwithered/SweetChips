package org.sweetchips.transformlauncher.bridge;

import java.util.Collection;

public interface TransformInvocation {

    Collection<TransformInput> getInputs();

    TransformOutputProvider getOutputProvider();

    boolean isIncremental();
}
