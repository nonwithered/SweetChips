package org.sweetchips.plugin4gradle;

import com.android.build.gradle.BaseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

public class UnionPlugin implements Plugin<Project> {

    private BaseExtension android;

    @Override
    public void apply(Project project) {
        init(project);
        UnionExtension extension = project.getExtensions().create(Util.NAME, UnionExtension.class);
        if (extension.isEnable()) {
            addTransform(Util.NAME, extension);
            extension.getMultiTransform().forEach((name) -> addTransform(name, extension));
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

    private void addTransform(String name, UnionExtension ext) {
        UnionContext context = new UnionContext(name, ext);
        Util.CONTEXTS.putIfAbsent(name, context);
        android.registerTransform(new UnionTransform(context));
    }
}
