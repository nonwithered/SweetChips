package org.sweetchips.inlinetailor;

import org.gradle.api.Project;
import org.sweetchips.android.AbstractPlugin;
import org.sweetchips.android.WorkflowSettings;
import org.sweetchips.common.jvm.ClassVisitorFactory;

public final class InlineTailorPlugin extends AbstractPlugin<InlineTailorExtension> {

    static InlineTailorPlugin INSTANCE;

    public InlineTailorPlugin() {
        super();
    }

    @Override
    protected final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    protected final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformLast(ClassVisitorFactory.fromClassNode(InlineTailorTransformClassNode.class));
    }
}