package org.sweetchips.transformlauncher.hook;

import java.util.Collection;

public interface TransformInvocation {

    Collection<TransformInput> getInputs();

    TransformOutputProvider getOutputProvider();

    boolean isIncremental();
}
