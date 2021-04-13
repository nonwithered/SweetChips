package org.sweetchips.recursivetail;

import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.ClassNodeAdaptor;
import org.sweetchips.platform.jvm.WorkflowSettings;

public final class RecursiveTailContext extends BasePluginContext {

    @Override
    public final void onAttach(WorkflowSettings settings) {
        settings.addTransformLast((api, cv, ext) -> new ClassNodeAdaptor(api, cv, new RecursiveTailClassNode(api).setContext(this)));
    }

    public static final String NAME = "RecursiveTail";
}
