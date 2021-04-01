package org.sweetchips.gradle.java;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.SweetChipsGradlePlugin;
import org.sweetchips.platform.jvm.JvmContext;

public final class SweetChipsJavaGradlePlugin extends SweetChipsGradlePlugin {

    static SweetChipsJavaGradlePlugin INSTANCE;
    private WorkflowActions mActions;

    @Override
    protected final void onApply(Project project) {
        super.onApply(project);
        init();
    }

    private void init() {
        mActions = new WorkflowActions(getProject().getTasks().getByName("classes"), getProject().getTasks().getByName("jar"));
    }

    @Override
    public void registerTransform(String name, JvmContext context) {
        mActions.registerTransform(new SweetChipsJavaTransform(name, context));
    }
}
