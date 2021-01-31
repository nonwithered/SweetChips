package org.sweetchips.plugin4gradle;

import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnionExtension {

    public UnionExtension() {
    }

    private boolean mIsIncremental = false;

    boolean isIncremental() {
        return mIsIncremental;
    }

    public void setIncremental(boolean isIncremental) {
        mIsIncremental = isIncremental;
    }

    private int mAsmApi = Opcodes.ASM5;

    int getAsmApi() {
        return mAsmApi;
    }

    public void setAsmApi(int asmApi) {
        mAsmApi = asmApi;
    }

    public void addTransform(String... name) {
        Arrays.stream(name).forEach(UnionContext.getPlugin()::addTransform);
    }
}
