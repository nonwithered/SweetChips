package org.sweetchips.constsweeper;

import org.objectweb.asm.Opcodes;
import org.sweetchips.plugin4gradle.util.ClassesUtil;

interface Util {

    String NAME = "ConstSweeper";

    static boolean checkDesc(String desc) {
        switch (desc) {
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
            case "J":
            case "F":
            case "D":
            case "Ljava/lang/String;":
                return true;
            default:
                return false;
        }
    }

    static boolean unusedField(int access, String name, String desc, String signature, Object value) {
        return value != null
                && ClassesUtil.checkAccess(access, Opcodes.ACC_STATIC)
                && ClassesUtil.checkAccess(access, Opcodes.ACC_FINAL)
                && Util.checkDesc(desc)
                && signature == null;
    }
}
