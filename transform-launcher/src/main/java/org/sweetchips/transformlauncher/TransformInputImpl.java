package org.sweetchips.transformlauncher;

import org.sweetchips.plugin4gradle.util.AsyncUtil;
import org.sweetchips.plugin4gradle.util.FilesUtil;
import org.sweetchips.transformlauncher.bridge.DirectoryInput;
import org.sweetchips.transformlauncher.bridge.JarInput;
import org.sweetchips.transformlauncher.bridge.TransformInput;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

final class TransformInputImpl implements TransformInput {

    private final Collection<JarInput> mJarInputs;

    private final Collection<DirectoryInput> mDirectoryInputs;

    TransformInputImpl(Path path, Path next) {
        if (!FilesUtil.exists(next)) {
            AsyncUtil.run(() -> FilesUtil.createDirectories(next));
        }
        mJarInputs = AsyncUtil.call(() -> FilesUtil.list(path))
                .filter(it -> FilesUtil.isRegularFile(it) && FilesUtil.getFileName(it).endsWith(".jar"))
                .map(it -> new JarInputImpl(it, next.resolve(it.getFileName())))
                .collect(Collectors.toList());
        mDirectoryInputs = AsyncUtil.call(() -> FilesUtil.list(path))
                .filter(FilesUtil::isDirectory)
                .map(it -> new DirectoryInputImpl(it, next.resolve(it.getFileName())))
                .collect(Collectors.toList());
        Set<String> pathNames = AsyncUtil.call(() -> FilesUtil.list(path))
                .map(FilesUtil::getFileName)
                .collect(Collectors.toSet());
        AsyncUtil.call(() -> FilesUtil.list(next))
                .filter(it -> !pathNames.contains(FilesUtil.getFileName(it)))
                .filter(it -> FilesUtil.isRegularFile(it) && FilesUtil.getFileName(it).endsWith(".jar"))
                .forEach(it -> mJarInputs.add(new JarInputImpl(path.resolve(it.getFileName()), it)));
        AsyncUtil.call(() -> FilesUtil.list(next))
                .filter(it -> !pathNames.contains(FilesUtil.getFileName(it)))
                .filter(FilesUtil::isDirectory)
                .forEach(it -> mDirectoryInputs.add(new DirectoryInputImpl(path.resolve(it.getFileName()), it)));
    }

    @Override
    public Collection<JarInput> getJarInputs() {
        return mJarInputs;
    }

    @Override
    public Collection<DirectoryInput> getDirectoryInputs() {
        return mDirectoryInputs;
    }
}
