package org.sweetchips.gradle.android;

import com.android.build.gradle.BaseExtension;

import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.gradle.common.SweetChipsGradlePlugin;

public final class SweetChipsAndroidGradlePlugin extends SweetChipsGradlePlugin {

    private BaseExtension mAndroid;

    @Override
    protected final void onApply() {
        if (getProject().getPlugins().findPlugin("com.android.application") == null
                && getProject().getPlugins().findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("android plugin should be enabled first",
                    new RuntimeException("android plugin should be enabled first"));
        }
        mAndroid = (BaseExtension) getProject().getExtensions().getByName("android");
    }

    @Override
    public void registerTransform(String name, JvmContext context) {
        mAndroid.registerTransform(new SweetChipsAndroidGradleTransform(name, context));
    }
}
