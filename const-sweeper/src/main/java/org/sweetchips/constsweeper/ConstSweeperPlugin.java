package org.sweetchips.constsweeper;

import org.gradle.api.Project;
import org.sweetchips.plugin4gradle.AbstractPlugin;

public final class ConstSweeperPlugin extends AbstractPlugin<ConstSweeperExtension> {

    public ConstSweeperPlugin() {
        super();
    }

    private static ConstSweeperPlugin sPlugin;

    static ConstSweeperPlugin getInstance() {
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
        addAction(ActionType.PREPARE, ActionMode.LAST, task, ConstSweeperPrepareClassVisitor.class);
        addAction(ActionType.TRANSFORM, ActionMode.LAST, task, ConstSweeperTransformClassVisitor.class);
    }
}