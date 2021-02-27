package org.sweetchips.plugin4gradle;

import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;

import org.gradle.api.Project;
import org.sweetchips.plugin4gradle.util.AsyncUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class Debugger {

    public static synchronized Debugger getInstance() {
        if (!isDebug()) {
            throw new IllegalStateException();
        }
        return sDebugger;
    }

    public static synchronized void setInstance(Class<? extends Debugger> clazz) {
        if (isDebug()) {
            throw new IllegalStateException();
        }
        try {
            sDebugger = clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        init();
    }

    protected Debugger() {
    }

    public final void launch() throws TransformException, InterruptedException, IOException {
        mTransforms.add(new UnionTransform(new UnionContext(null)));
        for (int i = 0; i < mTransforms.size(); i++) {
            Path src = i == 0 ? getInput() : getTransformsPath(mTransforms.get(i - 1));
            Path desc = i == mTransforms.size() - 1 ? getOutput() : getTransformsPath(mTransforms.get(i));
            mTransforms.get(i).transform(getInvocation(src, desc));
        }
    }

    protected abstract Project getProject();

    protected abstract Path getInput();

    protected abstract Path getOutput();

    protected abstract Path getIntermediatesPath();

    protected abstract TransformInvocation getInvocation(Path input, Path output);

    private static Debugger sDebugger;

    static boolean isDebug() {
        return sDebugger != null;
    }

    private static void init() {
        UnionPlugin.setInstance(new UnionPlugin());
        UnionPlugin.getInstance().setProject(getInstance().getProject());
        UnionPlugin.getInstance().setExtension(new UnionExtension());
    }

    private final List<Transform> mTransforms = new ArrayList<>();

    final void registerTransform(Transform transform) {
        mTransforms.add(transform);
    }

    private Path getTransformsPath() {
        Path transformsPath = getIntermediatesPath().resolve("transforms");
        if (!Files.isDirectory(transformsPath)) {
            AsyncUtil.run(() -> Files.createDirectories(transformsPath)).run();
        }
        return transformsPath;
    }

    private Path getTransformsPath(Transform transform) {
        String name = transform.getName();
        if (name == null) {
            throw new NullPointerException();
        }
        Path transformsPath = getTransformsPath().resolve(name);
        if (!Files.isDirectory(transformsPath)) {
            AsyncUtil.run(() -> Files.createDirectories(transformsPath)).run();
        }
        return transformsPath;
    }
}
