package org.sweetchips.platform.common;

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

    protected abstract void onPrepare();

    protected abstract void onTransform();
}
