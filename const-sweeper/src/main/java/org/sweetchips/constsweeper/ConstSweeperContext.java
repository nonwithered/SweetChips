package org.sweetchips.constsweeper;

import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.WorkflowSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConstSweeperContext extends BasePluginContext {

    @Override
    public final void onAttach(WorkflowSettings settings) {
        settings.addPrepareBefore(it -> it.put(ConstSweeperContext.NAME, new ConcurrentHashMap<String, Object>()));
        settings.addPrepareFirst((api, cv, ext) -> new ConstSweeperPrepareClassVisitor(api, cv).setContext(this));
        settings.addTransformFirst((api, cv, ext) -> new ConstSweeperTransformClassVisitor(api, cv).setContext(this));
        settings.addTransformAfter(it -> it.remove(ConstSweeperContext.NAME));
    }

    @Override
    public synchronized boolean isIgnored(String clazz, String member) {
        return super.isIgnored(clazz, member);
    }

    public static final String NAME = "ConstSweeper";

    private final Map<String, Object> mConstants = new ConcurrentHashMap<>();

    Map<String, Object> getConstants() {
        return mConstants;
    }
}
