package org.sweetchips.recursivetail;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.AbstractPlugin;
import org.sweetchips.gradle.common.WorkflowSettings;
import org.sweetchips.platform.jvm.ClassVisitorFactory;

public final class RecursiveTailPlugin extends AbstractPlugin<RecursiveTailExtension> {

    static RecursiveTailPlugin INSTANCE;

    @Override
    public final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformLast(ClassVisitorFactory.fromClassNode(RecursiveTailClassNode.class));
    }
}