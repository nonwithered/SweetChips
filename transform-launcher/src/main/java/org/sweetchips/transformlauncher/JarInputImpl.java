package org.sweetchips.transformlauncher;

import org.sweetchips.plugin4gradle.util.FilesUtil;
import org.sweetchips.transformlauncher.bridge.JarInput;
import org.sweetchips.transformlauncher.bridge.Status;

import java.io.File;
import java.nio.file.Path;

final class JarInputImpl implements JarInput {

    private final Path mPath;

    private final Status mStatus;

    JarInputImpl(Path path, Path next) {
        this.mPath = path;
        if (FilesUtil.exists(path)) {
            if (FilesUtil.exists(next)) {
                mStatus = Status.CHANGED;
            } else {
                mStatus = Status.ADDED;
            }
        } else if (FilesUtil.exists(next)) {
            mStatus = Status.REMOVED;
        } else {
            mStatus = Status.NOTCHANGED;
        }
    }

    @Override
    public Status getStatus() {
        return mStatus;
    }

    @Override
    public String getName() {
        return FilesUtil.getFileName(mPath);
    }

    @Override
    public File getFile() {
        return mPath.toFile();
    }
}
