package org.sweetchips.inlinetailor.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.inlinetailor.InlineTailorContext;
import org.sweetchips.inlinetailor.InlineTailorTransformClassNode;
import org.sweetchips.platform.jvm.ClassNodeAdaptor;
import org.sweetchips.platform.jvm.WorkflowSettings;

public final class InlineTailorGradlePlugin extends AbstractGradlePlugin<InlineTailorExtension> {

    @Override
    public final String getName() {
        return InlineTailorContext.NAME;
    }

    @Override
    protected final void onApply() {
    }

    @Override
    protected final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformLast((api, cv, ext) -> new ClassNodeAdaptor(api, cv, new InlineTailorTransformClassNode(api).setContext(getExtension().getContext())));
    }
}