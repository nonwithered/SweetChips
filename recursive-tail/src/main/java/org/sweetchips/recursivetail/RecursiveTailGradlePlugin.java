package org.sweetchips.recursivetail;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.platform.jvm.WorkflowSettings;
import org.sweetchips.platform.jvm.ClassNodeAdaptor;

public final class RecursiveTailGradlePlugin extends AbstractGradlePlugin<RecursiveTailExtension> {

    @Override
    public final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformLast((api, cv, ext) -> new ClassNodeAdaptor(api, cv, new RecursiveTailClassNode(api).withPlugin(this)));
    }
}