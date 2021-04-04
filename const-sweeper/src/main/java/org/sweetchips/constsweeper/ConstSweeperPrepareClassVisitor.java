package org.sweetchips.constsweeper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.sweetchips.platform.jvm.BaseClassVisitor;
import org.sweetchips.utility.ClassesUtil;

public final class ConstSweeperPrepareClassVisitor extends BaseClassVisitor<ConstSweeperContext> {

    private String mName;

    public ConstSweeperPrepareClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (!getContext().isIgnored(mName, name)
                && unusedField(access, desc, signature, value)) {
            getContext().getConstants().put(ClassesUtil.toStringField(mName, name, desc), value);
        }
        return super.visitField(access, name, desc, signature, value);
    }

    private static boolean unusedField(int access, String desc, String signature, Object value) {
        return value != null
                && checkAccess(access, Opcodes.ACC_STATIC)
                && checkAccess(access, Opcodes.ACC_FINAL)
                && checkDesc(desc)
                && signature == null;
    }

    private static boolean checkAccess(int access, int flag) {
        return (access & flag) != 0;
    }

    private static boolean checkDesc(String desc) {
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
}
