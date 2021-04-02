package org.sweetchips.traceweaver;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.platform.jvm.WorkflowSettings;

public final class TraceWeaverGradlePlugin extends AbstractGradlePlugin<TraceWeaverExtension> {

    @Override
    public final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformLast((api, cv, ext) -> new TraceWeaverTransformClassVisitor(api, cv).withPlugin(this));
        settings.addClass(() -> new TraceWrapperClassNode(settings.getAsmApi()));
    }
}