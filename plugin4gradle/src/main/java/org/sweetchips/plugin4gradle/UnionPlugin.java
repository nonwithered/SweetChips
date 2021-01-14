package org.sweetchips.plugin4gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class UnionPlugin implements Plugin<Project> {

    com.android.build.gradle.BaseExtension android;

    @Override
    public void apply(Project project) {
        android = findAndroid(project);
        UnionExtension extension = project.getExtensions().create(Util.NAME, UnionExtension.class);
        if (extension.isEnable()) {
            addTransform(Util.NAME, extension);
            extension.getMultiTransform().forEach((name) -> {
                BaseExtension ext = project.getExtensions().create(name, BaseExtension.class);
                if (ext.isEnable()) {
                    addTransform(name, ext);
                }
            });
        }
    }

    private com.android.build.gradle.BaseExtension findAndroid(Project project) {
        return (com.android.build.gradle.BaseExtension) project.getExtensions().getByName("android");
    }

    private void addTransform(String name, BaseExtension ext) {
        BaseContext context = new BaseContext(name, ext);
        Util.CONTEXTS.putIfAbsent(name, context);
        android.registerTransform(new UnionTransform(context));
    }
}
