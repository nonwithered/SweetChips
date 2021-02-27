package org.sweetchips.plugin4gradle;

import com.android.annotations.NonNull;
import com.android.build.api.transform.Transform;
import com.android.build.gradle.BaseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

public final class UnionPlugin implements Plugin<Project> {

    private static UnionPlugin sPlugin;

    static void setInstance(UnionPlugin plugin) {
        if (plugin == null) {
            throw new NullPointerException();
        }
        if (sPlugin != null) {
            throw new IllegalStateException();
        }
        sPlugin = plugin;
    }

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
        init(project);
        setProject(project);
        setExtension(project.getExtensions().create(Util.NAME, UnionExtension.class));
        addTransform(null);
    }

    public Project getProject() {
        return mProject;
    }

    public UnionExtension getExtension() {
        return mExtension;
    }

    void setProject(Project project) {
        mProject = project;
    }

    void setExtension(UnionExtension extension) {
        mExtension = extension;
    }

    private void init(Project project) {
        if (project.getPlugins().findPlugin("com.android.application") == null
                && project.getPlugins().findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("android plugin should be enabled first",
                    new RuntimeException("android plugin should be enabled first"));
        }
        android = findAndroid(project);
        setInstance(this);
    }

    private BaseExtension findAndroid(Project project) {
        return (BaseExtension) project.getExtensions().getByName("android");
    }

    void addTransform(String name) {
        Transform transform = new UnionTransform(UnionContext.getInstance(name));
        if (Debugger.isDebug()) {
            Debugger.getInstance().registerTransform(transform);
            return;
        }
        android.registerTransform(transform);
    }
}
