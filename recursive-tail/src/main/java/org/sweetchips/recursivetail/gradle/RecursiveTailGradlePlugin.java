package org.sweetchips.recursivetail.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.platform.jvm.ClassNodeAdaptor;
import org.sweetchips.platform.jvm.WorkflowSettings;
import org.sweetchips.recursivetail.RecursiveTailClassNode;
import org.sweetchips.recursivetail.RecursiveTailContext;

public final class RecursiveTailGradlePlugin extends AbstractGradlePlugin<RecursiveTailExtension> {

    @Override
    public final String getName() {
        return RecursiveTailContext.NAME;
    }

    @Override
    protected final void onApply() {
    }

    @Override
    protected final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformLast((api, cv, ext) -> new ClassNodeAdaptor(api, cv, new RecursiveTailClassNode(api).setContext(getExtension().getContext())));
    }
}