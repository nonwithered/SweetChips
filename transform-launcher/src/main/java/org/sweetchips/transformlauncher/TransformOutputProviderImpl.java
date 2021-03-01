package org.sweetchips.transformlauncher;

import org.sweetchips.transformlauncher.hook.Format;
import org.sweetchips.transformlauncher.hook.QualifiedContent;
import org.sweetchips.transformlauncher.hook.TransformOutputProvider;
import org.sweetchips.plugin4gradle.util.FilesUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

final class TransformOutputProviderImpl implements TransformOutputProvider {

    private final Path mPath;

    private final Path mNext;

    TransformOutputProviderImpl(Path path, Path next) {
        mPath = path;
        mNext = next;
    }

    @Override
    public File getContentLocation(String name,
                                   Set<QualifiedContent.ContentType> types,
                                   Set<? super QualifiedContent.Scope> scopes,
                                   Format format) {
        Path in = Paths.get(name);
        try {
            if (!Files.isSameFile(in.getParent().getParent(), mPath)) {
                throw new IOException();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        Path out = mNext.resolve(mPath.relativize(in));
        if (format == Format.DIRECTORY && !Files.exists(out)) {
            FilesUtil.createDirectories(out);
        }
        return out.toFile();
    }
}
