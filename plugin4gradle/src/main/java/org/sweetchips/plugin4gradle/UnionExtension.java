package org.sweetchips.plugin4gradle;

import org.objectweb.asm.Opcodes;

public class UnionExtension {

    private boolean isEnable = true;

    private boolean isIncremental = false;

    private int asmApi = Opcodes.ASM5;

    public UnionExtension() {
    }

    boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    boolean isIncremental() {
        return isIncremental;
    }

    public void setIncremental(boolean isIncremental) {
        this.isIncremental = isIncremental;
    }

    int getAsmApi() {
        return asmApi;
    }

    public void setAsmApi(int asmApi) {
        this.asmApi = asmApi;
    }

}
