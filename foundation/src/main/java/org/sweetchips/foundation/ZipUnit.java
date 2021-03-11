package org.sweetchips.foundation;

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
        mPrepare = prepare;
        mTransform = transform;
    }

    @Override
    protected void onPrepare() {
        if (mPrepare == null || mPrepare.size() == 0) {
            super.onPrepare();
            return;
        }
        AsyncUtil.with(() -> new ZipFile(getInput().toFile()))
                .with(it -> Collections.list(it.entries()).stream())
                .forkJoin((zip, it) -> {
                    Consumer<byte[]> consumer = null;
                    for (Function<ZipEntry, Consumer<byte[]>> filter : mPrepare) {
                        if ((consumer = filter.apply(it)) != null) {
                            break;
                        }
                    }
                    if (consumer != null) {
                        consumer.accept(FilesUtil.newZipReader(zip).readFromAsync(it));
                    }
                });
    }

    @Override
    protected void onTransform() {
        if (mTransform == null || mTransform.size() == 0) {
            super.onTransform();
            return;
        }
        ZipFile zip = AsyncUtil.call(() -> new ZipFile(getInput().toFile()));
        FilesUtil.ZipWriter writer = FilesUtil.newZipWriter(getOutput(), zip.size());
        AsyncUtil.with(Collections.list(zip.entries()).stream())
                .forEachAsync(it -> {
                    Function<byte[], byte[]> function = null;
                    for (Function<ZipEntry, Function<byte[], byte[]>> filter : mTransform) {
                        if ((function = filter.apply(it)) != null) {
                            break;
                        }
                    }
                    byte[] bytes = FilesUtil.newZipReader(zip).readFromAsync(it);
                    if (function != null) {
                        bytes = function.apply(bytes);
                    }
                    writer.writeTo(it.getName(), bytes);
                });
        AsyncUtil.managedBlock(writer);
    }
}
