package org.sweetchips.constsweeper.gradle;

import org.sweetchips.constsweeper.ConstSweeperContext;
import org.sweetchips.constsweeper.ConstSweeperPrepareClassVisitor;
import org.sweetchips.constsweeper.ConstSweeperTransformClassVisitor;
import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.platform.jvm.WorkflowSettings;

import java.util.concurrent.ConcurrentHashMap;

public final class ConstSweeperGradlePlugin extends AbstractGradlePlugin<ConstSweeperExtension> {

    @Override
    public final String getName() {
        return ConstSweeperContext.NAME;
    }

    @Override
    protected final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addPrepareBefore(it -> it.put(ConstSweeperContext.NAME, new ConcurrentHashMap<String, Object>()));
        settings.addPrepareFirst((api, cv, ext) -> new ConstSweeperPrepareClassVisitor(api, cv).setContext(getExtension().getContext()));
        settings.addTransformFirst((api, cv, ext) -> new ConstSweeperTransformClassVisitor(api, cv).setContext(getExtension().getContext()));
        settings.addTransformAfter(it -> it.remove(ConstSweeperContext.NAME));
    }
}