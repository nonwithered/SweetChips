package org.sweetchips.constsweeper;

import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

interface Util {

    String NAME = "ConstSweeper";

    Map<String, Object> sConstantValues = new ConcurrentHashMap<>();

    static boolean unusedField(int access, String name, String desc, String signature, Object value) {
        return value != null
                && checkAccess(access, Opcodes.ACC_STATIC)
                && checkAccess(access, Opcodes.ACC_FINAL)
                && checkDesc(desc)
                && signature == null;
    }

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

    static String getKey(String className, String fieldName, String fieldType) {
        return className + "->" + fieldName + ":" + fieldType;
    }
}
