package org.sweetchips.gradle.java;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.SweetChipsGradlePlugin;
import org.sweetchips.platform.jvm.JvmContext;

public final class SweetChipsJavaGradlePlugin extends SweetChipsGradlePlugin {

    private WorkflowActions mActions;

    @Override
    protected final void onApply() {
        mActions = new WorkflowActions(this, getProject().getTasks().getByName("classes"), getProject().getTasks().getByName("jar"));
    }

    @Override
    protected final void registerTransform(String name, JvmContext context) {
        mActions.registerTransform(new SweetChipsJavaGradleTransform(name, context));
    }
}
