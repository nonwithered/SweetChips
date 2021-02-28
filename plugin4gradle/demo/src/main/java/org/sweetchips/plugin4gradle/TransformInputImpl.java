package org.sweetchips.plugin4gradle;

import org.sweetchips.plugin4gradle.hook.DirectoryInput;
import org.sweetchips.plugin4gradle.hook.JarInput;
import org.sweetchips.plugin4gradle.hook.TransformInput;
import org.sweetchips.plugin4gradle.util.FilesUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

final class TransformInputImpl implements TransformInput {

    private final Collection<JarInput> mJarInputs;

    private final Collection<DirectoryInput> mDirectoryInputs;

    TransformInputImpl(Path path, Path next) {
        if (!Files.exists(next)) {
            FilesUtil.createDirectories(next);
        }
        mJarInputs = FilesUtil.list(path)
                .filter(it -> Files.isRegularFile(it) && FilesUtil.getFileName(it).endsWith(".jar"))
                .map(it -> new JarInputImpl(it, next.resolve(it.getFileName())))
                .collect(Collectors.toList());
        mDirectoryInputs = FilesUtil.list(path)
                .filter(Files::isDirectory)
                .map(it -> new DirectoryInputImpl(it, next.resolve(it.getFileName())))
                .collect(Collectors.toList());
        Set<String> pathNames = FilesUtil.list(path)
                .map(FilesUtil::getFileName)
                .collect(Collectors.toSet());
        FilesUtil.list(next)
                .filter(it -> !pathNames.contains(FilesUtil.getFileName(it)))
                .filter(it -> Files.isRegularFile(it) && FilesUtil.getFileName(it).endsWith(".jar"))
                .forEach(it -> mJarInputs.add(new JarInputImpl(path.resolve(it.getFileName()), it)));
        FilesUtil.list(next)
                .filter(it -> !pathNames.contains(FilesUtil.getFileName(it)))
                .filter(it -> Files.isDirectory(it))
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
