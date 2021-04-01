package org.sweetchips.sourcelineeraser;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.AbstractPlugin;
import org.sweetchips.gradle.common.WorkflowSettings;

public class SourceLineEraserPlugin extends AbstractPlugin<SourceLineEraserExtension> {

    @Override
    public final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addTransformFirst((api, cv, ext) -> new EraseSourceTransformClassVisitor(api, cv).withPlugin(this));
        settings.addTransformFirst((api, cv, ext) -> new EraseLineNumberTransformVisitor(api, cv).withPlugin(this));
    }
}
