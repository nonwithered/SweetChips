package org.sweetchips.plugin.gradle;

import org.sweetchips.common.jvm.JvmContext;

public class WorkflowExtension {

    private JvmContext mContext = new JvmContext();

    JvmContext transferContext() {
        JvmContext context = mContext;
        mContext = null;
        return context;
    }

    public boolean isIncremental() {
        JvmContext context = mContext;
        return context != null && context.isIncremental();
    }
}
