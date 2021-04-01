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

    private final Task mBefore;
    private final Task mSweep;
    private Task mLast;
    private final Queue<Map.Entry<String, SweetChipsJavaTransform>> mActions = new ArrayDeque<>();

    WorkflowActions(Task before, Task after) {
        mBefore = before;
        mSweep = SweetChipsJavaGradlePlugin.INSTANCE.getProject().task(getName(SweetChipsJavaGradlePlugin.INSTANCE.getName()));
        after.dependsOn(mSweep);
        mSweep.dependsOn(before);
        mSweep.doLast(this::sweep);
    }

    void registerTransform(SweetChipsJavaTransform transform) {
        String name = transform.getName();
        Task task = SweetChipsJavaGradlePlugin.INSTANCE.getProject().task(getName(name));
        mSweep.dependsOn(task);
        task.dependsOn(mActions.isEmpty() ? mBefore : mLast);
        mLast = task;
        mActions.offer(ItemsUtil.newPairEntry(name, transform));
        task.doLast(this::work);
    }

    private void work(Task task) {
        String name = fromTask(task.getName());
        String last = null;
        for (Map.Entry<String, SweetChipsJavaTransform> it : mActions) {
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
        SweetChipsJavaTransform transform = null;
        for (Map.Entry<String, SweetChipsJavaTransform> it : mActions) {
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
        context.setApi(SweetChipsJavaGradlePlugin.INSTANCE.getExtension().getAsmApi());
        new SweetChipsJavaTransform(SweetChipsJavaGradlePlugin.INSTANCE.getName(), context).transform(provider, from, paths);
    }

    private static String getName(String name) {
        return PREFIX + name;
    }

    private static String fromTask(String name) {
        return name.substring(PREFIX.length());
    }

    private static final String PREFIX = "transformClassesWith";

    private static Path getClassDir() {
        return SweetChipsJavaGradlePlugin.INSTANCE.getProject().getBuildDir().toPath()
                .resolve("classes");
    }

    private static Path getTempDir(String name) {
        return SweetChipsJavaGradlePlugin.INSTANCE.getProject().getBuildDir().toPath()
                .resolve("intermediates").resolve("transforms").resolve(name);
    }
}
