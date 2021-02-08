package org.sweetchips.plugin4gradle;

public abstract class AbstractExtension {

    private final AbstractPlugin<? extends AbstractExtension> mPlugin;

    private boolean mInit;

    public final void attach(String task) {
        if (mInit) {
            throw new IllegalStateException();
        } else {
            mInit = true;
        }
        mPlugin.onAttach(task);
    }

    protected AbstractExtension(AbstractPlugin<? extends AbstractExtension> plugin) {
        mPlugin = plugin;
    }
}
