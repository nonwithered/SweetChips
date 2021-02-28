package org.sweetchips.plugin4gradle;

import org.sweetchips.plugin4gradle.hook.JarInput;
import org.sweetchips.plugin4gradle.hook.Status;
import org.sweetchips.plugin4gradle.util.FilesUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

final class JarInputImpl implements JarInput {

    private final Path mPath;

    private final Status mStatus;

    JarInputImpl(Path path, Path next) {
        this.mPath = path;
        if (Files.exists(path)) {
            if (Files.exists(next)) {
                mStatus = Status.CHANGED;
            } else {
                mStatus = Status.ADDED;
            }
        } else if (Files.exists(next)) {
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
