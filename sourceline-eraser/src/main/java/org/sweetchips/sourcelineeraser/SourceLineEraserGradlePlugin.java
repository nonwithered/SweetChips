package org.sweetchips.sourcelineeraser;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.platform.jvm.WorkflowSettings;

public class SourceLineEraserGradlePlugin extends AbstractGradlePlugin<SourceLineEraserExtension> {

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
