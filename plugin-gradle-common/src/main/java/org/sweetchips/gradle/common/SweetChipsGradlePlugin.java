package org.sweetchips.gradle.common;

import org.gradle.api.Project;
import org.sweetchips.platform.jvm.JvmContext;

public abstract class SweetChipsGradlePlugin extends AbstractGradlePlugin<SweetChipsExtension> {

    @Override
    protected void onApply(Project project) {
    }

    @Override
    public final String getName() {
        return "SweetChips";
    }

    public abstract void registerTransform(String name, JvmContext context);
}
