package org.sweetchips.sourcelineeraser;

import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.WorkflowSettings;

public final class SourceLineEraserContext extends BasePluginContext {

    @Override
    public final void onAttach(WorkflowSettings settings) {
        settings.addTransformFirst((api, cv, ext) -> new EraseSourceTransformClassVisitor(api, cv).setContext(this));
        settings.addTransformFirst((api, cv, ext) -> new EraseLineNumberTransformVisitor(api, cv).setContext(this));
    }

    public static final String NAME = "SourceLineEraser";
}
