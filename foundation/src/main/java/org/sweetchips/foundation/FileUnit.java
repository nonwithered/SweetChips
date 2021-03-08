package org.sweetchips.foundation;

import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public final class FileUnit extends AbstractUnit {

    public static Function<Path, Consumer<byte[]>> prepareIgnore() {
        return p -> b -> {};
    }
    public static Function<Path, Function<byte[], byte[]>> transformIgnore() {
        return p -> b -> b;
    }

    private final List<Function<Path, Consumer<byte[]>>> mPrepare;
    private final List<Function<Path, Function<byte[], byte[]>>> mTransform;

    public FileUnit(Path input, Path output,
                    List<Function<Path, Consumer<byte[]>>> prepare,
                    List<Function<Path, Function<byte[], byte[]>>> transform) {
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
        Consumer<byte[]> consumer = null;
        for (Function<Path, Consumer<byte[]>> filter : mPrepare) {
            if ((consumer = filter.apply(getInput())) != null) {
                break;
            }
        }
        if (consumer != null) {
            Consumer<byte[]> c = consumer;
            AsyncUtil.managedBlock(() -> c.accept(FilesUtil.readFrom(getInput())));
        }
    }

    @Override
    protected void onTransform() {
        if (mTransform == null || mTransform.size() == 0) {
            super.onTransform();
            return;
        }
        Function<byte[], byte[]> function = null;
        for (Function<Path, Function<byte[], byte[]>> filter : mTransform) {
            if ((function = filter.apply(getInput())) != null) {
                break;
            }
        }
        Function<byte[], byte[]> f = function;
        AtomicReference<byte[]> bytes = new AtomicReference<>();
        AsyncUtil.managedBlock(() -> bytes.set(FilesUtil.readFrom(getInput())));
        if (f != null) {
            bytes.set(f.apply(bytes.get()));
        }
        FilesUtil.writeTo(getOutput(), bytes.get());
    }
}
