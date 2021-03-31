package org.sweetchips.platform.common;

import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ZipUnit extends AbstractUnit {

    private final List<Function<ZipEntry, Consumer<byte[]>>> mPrepare;
    private final List<Function<ZipEntry, Function<byte[], byte[]>>> mTransform;

    public ZipUnit(Path input, Path output,
                   List<Function<ZipEntry, Consumer<byte[]>>> prepare,
                   List<Function<ZipEntry, Function<byte[], byte[]>>> transform) {
        super(input, output);
        mPrepare = prepare != null ? prepare : Collections.emptyList();
        mTransform = transform != null ? transform : Collections.emptyList();
    }

    @Override
    protected final void onPrepare() {
        if (mPrepare.isEmpty()) {
            return;
        }
        ZipFile zipFile = AsyncUtil.call(() -> new ZipFile(getInput().toFile()));
        if (zipFile.size() == 0) {
            return;
        }
        AsyncUtil.with(zipFile)
                .with(it -> Collections.list(it.entries()).stream())
                .forkJoin((zip, it) -> {
                    Consumer<byte[]> consumer = null;
                    for (Function<ZipEntry, Consumer<byte[]>> filter : mPrepare) {
                        if ((consumer = filter.apply(it)) != null) {
                            break;
                        }
                    }
                    if (consumer != null) {
                        consumer.accept(FilesUtil.newZipReader(zip).readFrom(it));
                    }
                });
    }

    @Override
    protected final void onTransform() {
        ZipFile zip = AsyncUtil.call(() -> new ZipFile(getInput().toFile()));
        if (zip.size() == 0) {
            return;
        }
        FilesUtil.ZipWriter writer = FilesUtil.newZipWriter(getOutput(), zip.size());
        AsyncUtil.with(Collections.list(zip.entries()).stream())
                .forEachAsync(writer, it -> {
                    Function<byte[], byte[]> function = null;
                    for (Function<ZipEntry, Function<byte[], byte[]>> filter : mTransform) {
                        if ((function = filter.apply(it)) != null) {
                            break;
                        }
                    }
                    byte[] bytes = FilesUtil.newZipReader(zip).readFrom(it);
                    if (function != null) {
                        bytes = function.apply(bytes);
                    }
                    writer.writeTo(it.getName(), bytes);
                });
        AsyncUtil.managedBlock(writer);
    }
}
