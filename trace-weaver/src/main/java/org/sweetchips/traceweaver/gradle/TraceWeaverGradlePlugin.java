package org.sweetchips.traceweaver.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.platform.jvm.WorkflowSettings;
import org.sweetchips.traceweaver.TraceWeaverContext;
import org.sweetchips.traceweaver.TraceWeaverTransformClassVisitor;
import org.sweetchips.traceweaver.TraceWrapperClassNode;

public final class TraceWeaverGradlePlugin extends AbstractGradlePlugin<TraceWeaverExtension> {

    @Override
    public final String getName() {
        return TraceWeaverContext.NAME;
    }

    @Override
    protected final void onApply() {
    }

    @Override
    protected final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformLast((api, cv, ext) -> new TraceWeaverTransformClassVisitor(api, cv).setContext(getExtension().getContext()));
        settings.addClass(() -> new TraceWrapperClassNode(settings.getAsmApi()));
    }
}