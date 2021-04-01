package org.sweetchips.recursivetail;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.AbstractPlugin;
import org.sweetchips.gradle.common.WorkflowSettings;
import org.sweetchips.platform.jvm.ClassNodeAdaptor;

public final class RecursiveTailPlugin extends AbstractPlugin<RecursiveTailExtension> {

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