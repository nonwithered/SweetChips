package org.sweetchips.foundation;

import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;

public abstract class AbstractUnit {

    private final Path mInput;
    private final Path mOutput;

    protected AbstractUnit(Path input, Path output) {
        mInput = input;
        mOutput = output;
    }

    public final Path getInput() {
        return mInput;
    }

    public final Path getOutput() {
        return mOutput;
    }

    protected void onDelete() {
        FilesUtil.deleteIfExists(mOutput);
    }

    protected abstract void onPrepare();

    protected abstract void onTransform();
}
