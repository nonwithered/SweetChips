package org.sweetchips.constsweeper;

import org.gradle.api.Project;
import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.platform.jvm.WorkflowSettings;
import org.sweetchips.platform.jvm.ClassVisitorFactory;

import java.util.concurrent.ConcurrentHashMap;

public final class ConstSweeperGradlePlugin extends AbstractGradlePlugin<ConstSweeperExtension> {

    @Override
    public final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addPrepareBefore(it -> it.put(Util.NAME, new ConcurrentHashMap<String, Object>()));
        settings.addPrepareFirst((api, cv, ext) -> new ConstSweeperPrepareClassVisitor(api, cv, ext).withPlugin(this));
        settings.addTransformFirst(ClassVisitorFactory.fromClassVisitor(ConstSweeperTransformClassVisitor.class));
        settings.addTransformAfter(it -> it.remove(Util.NAME));
    }
}