package org.sweetchips.traceweaver;

import org.gradle.api.Project;
import org.sweetchips.plugin4gradle.AbstractPlugin;

public final class TraceWeaverPlugin extends AbstractPlugin<TraceWeaverExtension> {

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
    protected final void onAttach(String name) {
        sPlugin = this;
        addAction(ActionType.TRANSFORM, ActionMode.LAST, name, TraceWeaverClassVisitor.class);
        createClass(name, Util.TRACE_WRAPPER_CLASS_NAME, getExtension().getClassNode());
    }
}