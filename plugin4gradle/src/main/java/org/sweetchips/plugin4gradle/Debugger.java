package org.sweetchips.plugin4gradle;

import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;

import org.gradle.api.Project;
import org.sweetchips.plugin4gradle.util.AsyncUtil;
import org.sweetchips.plugin4gradle.util.ClassesUtil;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class Debugger {

    private static boolean mDebug;

    static boolean isDebug() {
        return mDebug;
    }

    public static void init(Project project, Path dir) {
        if (isDebug()) {
            throw new IllegalStateException();
        }
        if (project == null || dir == null) {
            throw new NullPointerException();
        }
        mDebug = true;
        sProject = project;
        sDir = dir;
        new UnionPlugin().apply(sProject);
    }

    private static boolean mLaunch;

    public static void launch(Path path, Path next, BiFunction<Path, Path, TransformInvocation> factory) {
        if (!isDebug() || mLaunch) {
            throw new IllegalStateException();
        }
        mLaunch = true;
        sTransforms.add(new UnionTransform(new UnionContext(null)));
        AsyncUtil.call(() -> transform(path, next, factory)).get();
    }

    private static Void transform(Path path, Path next, BiFunction<Path, Path, TransformInvocation> factory) throws TransformException, InterruptedException, IOException {
        for (int i = 0; i < sTransforms.size(); i++) {
            Path src = i == 0 ? path : getFile(sTransforms.get(i - 1));
            Path desc = i == sTransforms.size() - 1 ? next : getFile(sTransforms.get(i));
            sTransforms.get(i).transform(factory.apply(src, desc));
        }
        return null;
    }

    public static void applyPlugin(Class<? extends AbstractPlugin> clazz) {
        if (!isDebug() || mLaunch) {
            throw new IllegalStateException();
        }
        ClassesUtil.newInstance(ClassesUtil.getDeclaredConstructor(clazz)).apply(sProject);
    }

    private static final List<Transform> sTransforms = new ArrayList<>();

    static void registerTransform(Transform transform) {
        sTransforms.add(transform);
    }

    private static Project sProject;

    private static Project defaultProject() {
        return (Project) Proxy.newProxyInstance(
                Debugger.class.getClassLoader(),
                new Class<?>[]{Project.class},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException();
                });
    }

    private static Path sDir;

    private static Path defaultDir() {
        return Paths.get("build", "intermediates", "transforms");
    }

    private static Path getDir() {
        if (!Files.exists(sDir)) {
            AsyncUtil.run(() -> Files.createDirectories(sDir)).run();
        }
        return sDir;
    }

    private static Path getFile(Transform transform) {
        String name = transform.getName();
        if (name == null) {
            throw new NullPointerException();
        }
        Path transformsPath = getDir().resolve(name);
        if (!Files.exists(transformsPath)) {
            AsyncUtil.run(() -> Files.createDirectories(transformsPath)).run();
        }
        return transformsPath;
    }

    private Debugger() {
        throw new UnsupportedOperationException();
    }
}
