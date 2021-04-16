package org.sweetchips.inlinetailor;

import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.ClassNodeAdaptor;
import org.sweetchips.platform.jvm.WorkflowSettings;

public final class InlineTailorContext extends BasePluginContext {

    @Override
    public final void onAttach(WorkflowSettings settings) {
        settings.addTransformLast((api, cv, ext) -> new ClassNodeAdaptor(api, cv, new InlineTailorTransformClassNode(api).setContext(this)));
    }

    public static final String NAME = "InlineTailor";
}
