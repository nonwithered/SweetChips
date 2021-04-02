package org.sweetchips.gradle.java;

import org.gradle.api.Task;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.utility.FilesUtil;
import org.sweetchips.utility.ItemsUtil;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

final class WorkflowActions {

    private final SweetChipsJavaGradlePlugin mPlugin;
    private final Task mBefore;
    private final Task mSweep;
    private Task mLast;
    private final Queue<Map.Entry<String, SweetChipsJavaGradleTransform>> mActions = new ArrayDeque<>();

    WorkflowActions(SweetChipsJavaGradlePlugin plugin, Task before, Task after) {
        mPlugin = plugin;
        mBefore = before;
        mSweep = mPlugin.getProject().task(getName(mPlugin.getName()));
        after.dependsOn(mSweep);
        mSweep.dependsOn(before);
        mSweep.doLast(this::sweep);
    }

    void registerTransform(SweetChipsJavaGradleTransform transform) {
        String name = transform.getName();
        Task task = mPlugin.getProject().task(getName(name));
        mSweep.dependsOn(task);
        task.dependsOn(mActions.isEmpty() ? mBefore : mLast);
        mLast = task;
        mActions.offer(ItemsUtil.newPairEntry(name, transform));
        task.doLast(this::work);
    }

    private void work(Task task) {
        String name = fromTask(task.getName());
        String last = null;
        for (Map.Entry<String, SweetChipsJavaGradleTransform> it : mActions) {
            if (it.getKey().equals(name)) {
                break;
            }
            last = it.getKey();
        }
        Path from = last == null ? getClassDir() : getTempDir(last);
        Path to = getTempDir(name);
        FilesUtil.deleteIfExists(to);
        Collection<Path> paths = new ArrayList<>();
        FilesUtil.list(from).forEach(it -> paths.add(it.resolve("main")));
        Function<Path, Path> provider = it -> to.resolve(from.relativize(it));
        SweetChipsJavaGradleTransform transform = null;
        for (Map.Entry<String, SweetChipsJavaGradleTransform> it : mActions) {
            if (it.getKey().equals(name)) {
                transform = it.getValue();
                break;
            }
        }
        if (transform == null) {
            throw new IllegalStateException();
        }
        transform.transform(provider, from, paths);
    }

    private void sweep(Task task) {
        if (mLast == null) {
            return;
        }
        Path from = getTempDir(fromTask(mLast.getName()));
        Path to = getClassDir();
        FilesUtil.deleteIfExists(to);
        Collection<Path> paths = new ArrayList<>();
        FilesUtil.list(from).forEach(it -> paths.add(it.resolve("main")));
        Function<Path, Path> provider = it -> to.resolve(from.relativize(it));
        JvmContext context = new JvmContext();
        context.setApi(mPlugin.getExtension().getAsmApi());
        new SweetChipsJavaGradleTransform(mPlugin.getName(), context).transform(provider, from, paths);
    }

    private Path getClassDir() {
        return mPlugin.getProject().getBuildDir().toPath()
                .resolve("classes");
    }

    private Path getTempDir(String name) {
        return mPlugin.getProject().getBuildDir().toPath()
                .resolve("intermediates")
                .resolve("transforms")
                .resolve(name);
    }

    private static String getName(String name) {
        return PREFIX + name;
    }

    private static String fromTask(String name) {
        return name.substring(PREFIX.length());
    }

    private static final String PREFIX = "transformClassesWith";
}
