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
        if (Debugger.isDebug()) {
            Debugger.registerTransform(newTransform(name));
            return;
        }
        android.registerTransform(newTransform(name));
    }

    private void initAndroid() {
        if (Debugger.isDebug()) {
            return;
        }
        if (getProject().getPlugins().findPlugin("com.android.application") == null
                && getProject().getPlugins().findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("android plugin should be enabled first",
                    new RuntimeException("android plugin should be enabled first"));
        }
        android = (BaseExtension) getProject().getExtensions().getByName("android");
    }

    private static UnionTransform newTransform(String name) {
        return new UnionTransform(UnionContext.getInstance(name));
    }

    private UnionExtension newExtension() {
        if (Debugger.isDebug()) {
            return new UnionExtension();
        }
        return getProject().getExtensions().create(Util.NAME, UnionExtension.class);
    }
}
