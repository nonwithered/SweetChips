package org.sweetchips.inlinetailor;

import org.objectweb.asm.tree.InsnList;
import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.ClassNodeAdaptor;
import org.sweetchips.platform.jvm.WorkflowSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InlineTailorPlusContext extends BasePluginContext {

    @Override
    public void onAttach(WorkflowSettings settings) {
        settings.addPrepareLast((api, cv, ext) -> new ClassNodeAdaptor(api, cv, new InlineTailorPlusPrepareClassNode(api).setContext(this)));
        settings.addTransformLast((api, cv, ext) -> new ClassNodeAdaptor(api, cv, new InlineTailorPlusTransformClassNode(api).setContext(this)));
    }

    public static final String NAME = "InlineTailorPlus";

    private final Map<String, Map.Entry<InsnList, Integer>> mItems = new ConcurrentHashMap<>();

    Map<String, Map.Entry<InsnList, Integer>> getItems() {
        return mItems;
    }
}
