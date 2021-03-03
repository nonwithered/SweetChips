package org.sweetchips.plugin4gradle;

import com.android.annotations.NonNull;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.plugin4gradle.util.ClassesUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractPlugin<Ext extends AbstractExtension> implements Plugin<Project> {

    protected AbstractPlugin() {
    }

    protected enum ActionMode {
        FIRST, LAST
    }

    protected enum ActionType {
        PREPARE, TRANSFORM
    }

    private Ext mExtension;

    private Project mProject;

    private boolean mInit;

    public final Ext getExtension() {
        return mExtension;
    }

    public final Project getProject() {
        return mProject;
    }

    public final int getAsmApi() {
        return UnionPlugin.getInstance().getExtension().getAsmApi();
    }

    @Override
    public final void apply(@NonNull Project project) {
        init(project);
        onApply(project);
    }

    protected abstract String getName();

    protected abstract void onApply(Project project);

    protected abstract void onAttach(String task);

    protected final void defineNewClass(String task, String name, Supplier<ClassNode> cn) {
        UnionContext.defineNewClass(task, name, cn);
    }

    protected final void defineNewClassCallback(String task, Consumer<ClassNode> callback) {
        UnionContext.defineNewClassCallback(task, callback);
    }

    protected final void addInitialize(String task, Runnable callback) {
        UnionContext.addInitialize(task, callback);
    }

    protected final void addRelease(String task, Runnable callback) {
        UnionContext.addRelease(task, callback);
    }

    protected final void addAction(ActionType type, ActionMode mode, String task, Class<? extends ClassVisitor> action) {
        UnionContext.addClassVisitor(type, mode, task, action);
    }

    private void init(Project project) {
        if (UnionPlugin.getInstance() == null) {
            throw new ProjectConfigurationException(getName(),
                    new IllegalStateException(Util.NAME + " plugin should be enabled first"));
        }
        if (mInit) {
            throw new IllegalStateException();
        } else {
            mInit = true;
        }
        mProject = project;
        Type type = getClass().getGenericSuperclass();
        ClassesUtil.checkAssert(type instanceof ParameterizedType);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        ClassesUtil.checkAssert(parameterizedType.getActualTypeArguments().length == 1);
        @SuppressWarnings("unchecked")
        Class<Ext> clazz = (Class<Ext>) parameterizedType.getActualTypeArguments()[0];
        mExtension = project.getExtensions().create(getName(), clazz, this);
    }
}
