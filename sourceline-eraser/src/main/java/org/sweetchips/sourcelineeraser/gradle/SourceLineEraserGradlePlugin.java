package org.sweetchips.sourcelineeraser.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.platform.jvm.WorkflowSettings;
import org.sweetchips.sourcelineeraser.EraseLineNumberTransformVisitor;
import org.sweetchips.sourcelineeraser.EraseSourceTransformClassVisitor;
import org.sweetchips.sourcelineeraser.SourceLineEraserContext;

public class SourceLineEraserGradlePlugin extends AbstractGradlePlugin<SourceLineEraserExtension> {

    @Override
    public final String getName() {
        return SourceLineEraserContext.NAME;
    }

    @Override
    protected final void onApply() {
    }

    @Override
    protected final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformFirst((api, cv, ext) -> new EraseSourceTransformClassVisitor(api, cv).setContext(getExtension().getContext()));
        settings.addTransformFirst((api, cv, ext) -> new EraseLineNumberTransformVisitor(api, cv).setContext(getExtension().getContext()));
    }
}
