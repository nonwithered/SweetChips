package org.sweetchips.constsweeper;

import org.gradle.api.Project;
import org.sweetchips.android.AbstractPlugin;
import org.sweetchips.android.WorkflowSettings;
import org.sweetchips.common.jvm.ClassVisitorFactory;

public final class ConstSweeperPlugin extends AbstractPlugin<ConstSweeperExtension> {

    static ConstSweeperPlugin INSTANCE;

    @Override
    protected final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    protected final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformFirst(ClassVisitorFactory.fromClassVisitor(ConstSweeperTransformClassVisitor.class));
    }
}