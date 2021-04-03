package org.sweetchips.traceweaver.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.traceweaver.TraceWeaverContext;

public final class TraceWeaverGradlePlugin extends AbstractGradlePlugin<TraceWeaverExtension> {

    @Override
    protected final String getName() {
        return TraceWeaverContext.NAME;
    }
}