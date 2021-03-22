package org.sweetchips.android;

import com.android.build.gradle.BaseExtension;

import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

public class SweetChipsPlugin extends AbstractPlugin<SweetChipsExtension> {

    static SweetChipsPlugin INSTANCE;
    BaseExtension android;

    @Override
    protected final String getName() {
        return "SweetChips";
    }

    @Override
    protected final void onApply(Project project) {
        initAndroid();
    }

    private void initAndroid() {
        if (getProject().getPlugins().findPlugin("com.android.application") == null
                && getProject().getPlugins().findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("android plugin should be enabled first",
                    new RuntimeException("android plugin should be enabled first"));
        }
        android = (BaseExtension) getProject().getExtensions().getByName("android");
    }
}
