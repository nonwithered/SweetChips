package org.sweetchips.constsweeper;

import org.gradle.api.Project;
import org.sweetchips.android.AbstractPlugin;
import org.sweetchips.android.WorkflowSettings;
import org.sweetchips.common.jvm.ClassVisitorFactory;

import java.util.concurrent.ConcurrentHashMap;

public final class ConstSweeperPlugin extends AbstractPlugin<ConstSweeperExtension> {

    static ConstSweeperPlugin INSTANCE;

    @Override
    protected final String getName() {
        return Util.NAME;
    }

    @Override
    protected final void onApply(Project project) {
    }

    protected final void onAttach(String name) {
        WorkflowSettings settings = getWorkflowSettings(name);
        settings.addPrepareBefore(it -> it.put(Util.NAME, new ConcurrentHashMap<String, Object>()));
        settings.addPrepareFirst(ClassVisitorFactory.fromClassVisitor(ConstSweeperPrepareClassVisitor.class));
        settings.addTransformFirst(ClassVisitorFactory.fromClassVisitor(ConstSweeperTransformClassVisitor.class));
        settings.addTransformAfter(it -> it.remove(Util.NAME));
    }
}