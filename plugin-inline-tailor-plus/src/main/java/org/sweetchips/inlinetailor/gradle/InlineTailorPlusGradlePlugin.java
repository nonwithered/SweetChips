package org.sweetchips.inlinetailor.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.inlinetailor.InlineTailorPlusContext;

public class InlineTailorPlusGradlePlugin extends AbstractGradlePlugin<InlineTailorPlusGradleExtension> {

    @Override
    protected final String getName() {
        return InlineTailorPlusContext.NAME;
    }
}
