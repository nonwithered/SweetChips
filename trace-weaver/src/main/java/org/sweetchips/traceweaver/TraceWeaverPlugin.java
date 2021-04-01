package org.sweetchips.traceweaver;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.AbstractPlugin;
import org.sweetchips.gradle.common.WorkflowSettings;

public final class TraceWeaverPlugin extends AbstractPlugin<TraceWeaverExtension> {

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