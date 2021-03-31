package org.sweetchips.gradle.java;

import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.sweetchips.common.jvm.JvmContext;
import org.sweetchips.gradle.common.SweetChipsPlugin;

public final class SweetChipsJavaPlugin extends SweetChipsPlugin {

    static SweetChipsJavaPlugin INSTANCE;
    private WorkflowActions mActions;

    @Override
    protected final void onApply(Project project) {
        super.onApply(project);
        init();
    }

    private void init() {
        if (getProject().getPlugins().findPlugin("java") == null
                && getProject().getPlugins().findPlugin("java-library") == null) {
            throw new ProjectConfigurationException("java plugin should be enabled first",
                    new RuntimeException("java plugin should be enabled first"));
        }
        mActions = new WorkflowActions(getProject().getTasks().getByName("classes"), getProject().getTasks().getByName("jar"));
    }

    @Override
    public void registerTransform(String name, JvmContext context) {
        mActions.registerTransform(new SweetChipsJavaTransform(name, context));
    }
}
