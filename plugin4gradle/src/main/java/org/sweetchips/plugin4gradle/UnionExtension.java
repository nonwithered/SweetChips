package org.sweetchips.plugin4gradle;

import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.List;

public class UnionExtension {

    public UnionExtension() {
    }

    private boolean mIsEnable = true;

    boolean isEnable() {
        return mIsEnable;
    }

    public void setEnable(boolean isEnable) {
        mIsEnable = isEnable;
    }

    private boolean mIsIncremental = false;

    boolean isIncremental() {
        return mIsIncremental;
    }

    public void setIncremental(boolean isIncremental) {
        mIsIncremental = isIncremental;
    }

    private List<String> mMultiTransform = Collections.emptyList();

    public void setMultiTransform(List<String> list) {
        mMultiTransform = list;
    }

    List<String> getMultiTransform() {
        return mMultiTransform;
    }

    private int mAsmApi = Opcodes.ASM5;

    public void setAsmApi(int asmApi) {
        mAsmApi = asmApi;
    }

    int getAsmApi() {
        return mAsmApi;
    }
}
