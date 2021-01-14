package org.sweetchips.plugin4gradle;

import org.objectweb.asm.Opcodes;

public class BaseExtension {

    private boolean isEnable = true;

    private boolean isIncremental = false;

    public BaseExtension() {
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
}
