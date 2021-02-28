package org.sweetchips.plugin4gradle;

import com.android.annotations.NonNull;
import com.android.build.gradle.BaseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

public final class UnionPlugin implements Plugin<Project> {

    private static UnionPlugin sPlugin;

    static UnionPlugin getInstance() {
        if (sPlugin == null) {
            throw new IllegalStateException();
        }
        return sPlugin;
    }

    private BaseExtension android;

    private Project mProject;

    private UnionExtension mExtension;

    @Override
    public void apply(@NonNull Project project) {
        mProject = project;
        sPlugin = this;
        mExtension = newExtension();
        initAndroid();
        addTransform(null);
    }

    public Project getProject() {
        return mProject;
    }

    public UnionExtension getExtension() {
        return mExtension;
    }

    void addTransform(String name) {
        android.registerTransform(newTransform(name));
    }

    private void initAndroid() {
        if (getProject().getPlugins().findPlugin(Util.APPLICATION) == null
                && getProject().getPlugins().findPlugin(Util.LIBRARY) == null) {
            throw new ProjectConfigurationException("android plugin should be enabled first",
                    new RuntimeException("android plugin should be enabled first"));
        }
        android = (BaseExtension) getProject().getExtensions().getByName(Util.ANDROID);
    }

    private static UnionTransform newTransform(String name) {
        return new UnionTransform(UnionContext.getInstance(name));
    }

    private UnionExtension newExtension() {
        return getProject().getExtensions().create(Util.NAME, UnionExtension.class);
    }
}
