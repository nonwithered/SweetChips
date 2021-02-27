package org.sweetchips.traceweaver;

import org.gradle.api.Project;
import org.sweetchips.plugin4gradle.AbstractPlugin;

public final class TraceWeaverPlugin extends AbstractPlugin<TraceWeaverExtension> {

    public TraceWeaverPlugin() {
        super();
    }

    private static TraceWeaverPlugin sPlugin;

    static TraceWeaverPlugin getInstance() {
        return sPlugin;
    }

    @Override
    protected final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
        getExtension().setClassNode(new TraceWrapperClassNode(getAsmApi()));
    }

    @Override
    protected final void onAttach(String task) {
        sPlugin = this;
        addAction(ActionType.TRANSFORM, ActionMode.LAST, task, TraceWeaverTransformClassVisitor.class);
        createClass(task, Util.TRACE_WRAPPER_CLASS_NAME, getExtension().getClassNode());
    }
}