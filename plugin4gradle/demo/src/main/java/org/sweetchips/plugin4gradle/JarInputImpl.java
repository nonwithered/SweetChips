package org.sweetchips.plugin4gradle;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.Status;

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

    @Override
    @SuppressWarnings("unchecked")
    public Set<ContentType> getContentTypes() {
        return (Set<ContentType>) Collections.EMPTY_SET;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<? super Scope> getScopes() {
        return (Set<? super Scope>) Collections.EMPTY_SET;
    }
}
