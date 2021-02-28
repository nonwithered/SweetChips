package org.sweetchips.plugin4gradle.hook;

import java.util.Collection;

public interface TransformInvocation {

    Collection<TransformInput> getInputs();

    TransformOutputProvider getOutputProvider();

    boolean isIncremental();
}
