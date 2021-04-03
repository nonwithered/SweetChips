package org.sweetchips.inlinetailor.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.inlinetailor.InlineTailorContext;

public final class InlineTailorGradlePlugin extends AbstractGradlePlugin<InlineTailorExtension> {

    @Override
    protected final String getName() {
        return InlineTailorContext.NAME;
    }
}