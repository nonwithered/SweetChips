package org.sweetchips.plugin4gradle;

import com.android.annotations.NonNull;
import com.android.build.gradle.BaseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

public class UnionPlugin implements Plugin<Project> {

    private BaseExtension android;

    @Override
    public void apply(@NonNull Project project) {
        init(project);
        UnionExtension extension = project.getExtensions().create(Util.NAME, UnionExtension.class);
        if (extension.isEnable()) {
            addTransform(project, null, extension);
            extension.getMultiTransform().forEach((name) -> addTransform(project, name, extension));
        }
    }

    private void init(Project project) {
        if (project.getPlugins().findPlugin("com.android.application") == null
                && project.getPlugins().findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("android plugin should be enabled first",
                    new IllegalStateException("android plugin should be enabled first"));
        }
        android = findAndroid(project);
    }

    private BaseExtension findAndroid(Project project) {
        return (BaseExtension) project.getExtensions().getByName("android");
    }

    private void addTransform(Project project, String name, UnionExtension extension) {
        android.registerTransform(new UnionTransform(UnionContext.newInstance(project, name, extension)));
    }
}
