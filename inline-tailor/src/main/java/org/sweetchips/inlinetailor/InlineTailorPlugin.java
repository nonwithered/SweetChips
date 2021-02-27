package org.sweetchips.inlinetailor;

import org.gradle.api.Project;
import org.sweetchips.plugin4gradle.AbstractPlugin;

public final class InlineTailorPlugin extends AbstractPlugin<InlineTailorExtension> {

    private static InlineTailorPlugin sPlugin;

    static InlineTailorPlugin getInstance() {
        return sPlugin;
    }

    @Override
    protected final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    @Override
    protected final void onAttach(String task) {
        sPlugin = this;
        addAction(ActionType.TRANSFORM, ActionMode.LAST, task, InlineTailorTransformClassNode.class);
    }
}