package org.sweetchips.transformlauncher;

import org.sweetchips.plugin4gradle.util.AsyncUtil;
import org.sweetchips.plugin4gradle.util.FilesUtil;
import org.sweetchips.transformlauncher.bridge.Format;
import org.sweetchips.transformlauncher.bridge.QualifiedContent;
import org.sweetchips.transformlauncher.bridge.TransformOutputProvider;

import java.io.File;
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
        if (!in.getParent().getParent().toAbsolutePath().toString().equals(mPath.toAbsolutePath().toString())) {
            throw new IllegalArgumentException(name);
        }
        Path out = mNext.resolve(mPath.relativize(in));
        if (format == Format.DIRECTORY && !FilesUtil.exists(out)) {
            AsyncUtil.run(() -> FilesUtil.createDirectories(out));
        }
        return out.toFile();
    }
}
