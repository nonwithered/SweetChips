package org.sweetchips.constsweeper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.sweetchips.platform.jvm.BaseClassVisitor;
import org.sweetchips.utility.ClassesUtil;

public final class ConstSweeperTransformClassVisitor extends BaseClassVisitor<ConstSweeperContext> {

    private static final String TAG = "ConstSweeperTransformClassVisitor";

    private String mName;

    public ConstSweeperTransformClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (getContext().getConstants().containsKey(ClassesUtil.toStringField(mName, name, desc))) {
            getContext().getLogger().i(TAG, "remove " + ClassesUtil.toStringField(mName, name, desc));
            return null;
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
            private final String mStr = ClassesUtil.toStringMethod(mName, name, desc);
            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (opcode == Opcodes.GETSTATIC) {
                    String str = ClassesUtil.toStringField(owner, name, desc);
                    Object value = getContext().getConstants().get(str);
                    if (value != null) {
                        super.visitLdcInsn(value);
                        getContext().getLogger().i(TAG, mStr + " inline constant " + str);
                        return;
                    }
                }
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        };
    }
}
