package org.sweetchips.traceweaver;

import org.gradle.api.Project;
import org.sweetchips.plugin4gradle.BasePlugin;

public final class TraceWeaverPlugin extends BasePlugin {

    private boolean mInit;

    @Override
    protected final void onApply(Project project) {
        TraceWeaverExtension extension = project.getExtensions().create(Util.NAME, TraceWeaverExtension.class);
        TraceWeaverContext.setExtension(extension);
        TraceWeaverContext.setPlugin(this);
        TraceWeaverContext.setProject(project);
    }

    void attach(String name) {
        if (mInit) {
            throw new IllegalStateException();
        } else {
            mInit = true;
        }
        addLastTransform(name, TraceWeaverClassVisitor.class);
        createClass(name, Util.TRACE_WRAPPER_CLASS_NAME, new TraceWrapperClassNode(getAsmApi()));
    }
}