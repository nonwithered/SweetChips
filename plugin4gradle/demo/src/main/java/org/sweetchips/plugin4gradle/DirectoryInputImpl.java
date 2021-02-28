package org.sweetchips.plugin4gradle;

import org.sweetchips.plugin4gradle.hook.DirectoryInput;
import org.sweetchips.plugin4gradle.hook.Status;
import org.sweetchips.plugin4gradle.util.FilesUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class DirectoryInputImpl implements DirectoryInput {

    private final Path mPath;

    private final Map<File, Status> mChangedFiles;

    @SuppressWarnings("unchecked")
    DirectoryInputImpl(Path path, Path next) {
        this.mPath = path;
        mChangedFiles = new HashMap<>();
        Set<String> pathNames = !Files.exists(path) ? Collections.EMPTY_SET
                : FilesUtil.list(path)
                .map(FilesUtil::getFileName)
                .collect(Collectors.toSet());
        Set<String> nextNames = !Files.exists(next) ? Collections.EMPTY_SET
                : FilesUtil.list(next)
                .map(FilesUtil::getFileName)
                .collect(Collectors.toSet());
        if (!pathNames.isEmpty()) {
            FilesUtil.list(path)
                    .filter(it -> nextNames.contains(FilesUtil.getFileName(it)))
                    .forEach(it -> mChangedFiles.put(it.toFile(), Status.CHANGED));
            FilesUtil.list(path)
                    .filter(it -> !nextNames.contains(FilesUtil.getFileName(it)))
                    .forEach(it -> mChangedFiles.put(it.toFile(), Status.ADDED));
        }
        if (!nextNames.isEmpty()) {
            nextNames.stream()
                    .filter(it -> !pathNames.contains(it))
                    .forEach(it -> mChangedFiles.put(path.resolve(it).toFile(), Status.REMOVED));
        }
    }

    @Override
    public Map<File, Status> getChangedFiles() {
        return mChangedFiles;
    }

    @Override
    public String getName() {
        return mPath.toAbsolutePath().toString();
    }

    @Override
    public File getFile() {
        return mPath.toFile();
    }
}
