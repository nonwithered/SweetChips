package org.sweetchips.constsweeper.maven;

import org.sweetchips.constsweeper.ConstSweeperContext;
import org.sweetchips.constsweeper.ConstSweeperPrepareClassVisitor;
import org.sweetchips.constsweeper.ConstSweeperTransformClassVisitor;
import org.sweetchips.maven.common.AbstractMavenPlugin;
import org.sweetchips.platform.jvm.WorkflowSettings;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

final class ConstSweeperMavenPlugin extends AbstractMavenPlugin<ConstSweeperContext> {

    public ConstSweeperMavenPlugin(String name, int asmApi, File basedir) {
        super(name, asmApi, basedir);
    }

    @Override
    protected final void onExecute(WorkflowSettings settings) {
        settings.addPrepareBefore(it -> it.put(ConstSweeperContext.NAME, new ConcurrentHashMap<String, Object>()));
        settings.addPrepareFirst((api, cv, ext) -> new ConstSweeperPrepareClassVisitor(api, cv).setContext(getContext()));
        settings.addTransformFirst((api, cv, ext) -> new ConstSweeperTransformClassVisitor(api, cv).setContext(getContext()));
        settings.addTransformAfter(it -> it.remove(ConstSweeperContext.NAME));
    }
}
