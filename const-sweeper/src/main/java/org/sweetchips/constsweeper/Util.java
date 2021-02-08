package org.sweetchips.constsweeper;

import org.objectweb.asm.Opcodes;

interface Util {

    String NAME = "ConstSweeper";

    static boolean checkAccess(int access, int flag) {
        return (access & flag) != 0;
    }

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
                && Util.checkAccess(access, Opcodes.ACC_STATIC)
                && Util.checkAccess(access, Opcodes.ACC_FINAL)
                && Util.checkDesc(desc)
                && signature == null;
    }
}
