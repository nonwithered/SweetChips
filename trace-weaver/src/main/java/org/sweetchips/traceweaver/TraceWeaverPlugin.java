package org.sweetchips.traceweaver;

import org.gradle.api.Project;
import org.sweetchips.common.jvm.ClassVisitorFactory;
import org.sweetchips.gradle.AbstractPlugin;
import org.sweetchips.gradle.WorkflowSettings;

public final class TraceWeaverPlugin extends AbstractPlugin<TraceWeaverExtension> {

    static TraceWeaverPlugin INSTANCE;

    @Override
    public final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformLast(ClassVisitorFactory.fromClassVisitor(TraceWeaverTransformClassVisitor.class));
        int asmApi = settings.getAsmApi();
        settings.addClass(() -> new TraceWrapperClassNode(asmApi));
    }
}