package org.sweetchips.gradle.android;

import com.android.build.gradle.BaseExtension;

import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.gradle.common.SweetChipsPlugin;

public final class SweetChipsAndroidPlugin extends SweetChipsPlugin {

    private BaseExtension mAndroid;

    @Override
    protected final void onApply(Project project) {
        super.onApply(project);
        init();
    }

    private void init() {
        if (getProject().getPlugins().findPlugin("com.android.application") == null
                && getProject().getPlugins().findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("android plugin should be enabled first",
                    new RuntimeException("android plugin should be enabled first"));
        }
        mAndroid = (BaseExtension) getProject().getExtensions().getByName("android");
    }

    @Override
    public void registerTransform(String name, JvmContext context) {
        mAndroid.registerTransform(new SweetChipsAndroidTransform(name, context));
    }
}
