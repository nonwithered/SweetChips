package org.sweetchips.plugin4gradle;

import com.android.annotations.NonNull;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public abstract class BasePlugin implements Plugin<Project> {

    @Override
    public final void apply(@NonNull Project project) {
        init(project);
        onApply(project);
    }

    protected abstract void onApply(Project project);

    protected final int getAsmApi() {
        return UnionContext.getExtension().getAsmApi();
    }

    protected final void createClass(String task, String name, ClassNode cn) {
        UnionContext.createClass(task, name, cn);
    }

    protected final void addFirstPrepare(String task, Class<? extends ClassVisitor> cv) {
        UnionContext.addFirstPrepare(task, cv);
    }

    protected final void addLastPrepare(String task, Class<? extends ClassVisitor> cv) {
        UnionContext.addLastPrepare(task, cv);
    }

    protected final void addFirstTransform(String task, Class<? extends ClassVisitor> cv) {
        UnionContext.addFirstTransform(task, cv);
    }

    protected final void addLastTransform(String task, Class<? extends ClassVisitor> cv) {
        UnionContext.addLastTransform(task, cv);
    }

    private void init(Project project) {
        if (UnionContext.getProject() == null) {
            throw new ProjectConfigurationException(Util.NAME + " plugin should be enabled first",
                    new RuntimeException(Util.NAME + " plugin should be enabled first"));
        }
    }
}
