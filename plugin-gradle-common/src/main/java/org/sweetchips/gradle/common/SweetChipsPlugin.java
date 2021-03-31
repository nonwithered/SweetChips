package org.sweetchips.gradle.common;

import org.gradle.api.Project;
import org.sweetchips.common.jvm.JvmContext;
import org.sweetchips.gradle.AbstractPlugin;

public abstract class SweetChipsPlugin extends AbstractPlugin<SweetChipsExtension> {

    @Override
    protected void onApply(Project project) {
        getExtension().setPlugin(this);
    }

    @Override
    public final String getName() {
        return "SweetChips";
    }

    public abstract void registerTransform(String name, JvmContext context);
}
