package org.sweetchips.recursivetail.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.recursivetail.RecursiveTailContext;

public final class RecursiveTailGradlePlugin extends AbstractGradlePlugin<RecursiveTailExtension> {

    @Override
    protected final String getName() {
        return RecursiveTailContext.NAME;
    }
}