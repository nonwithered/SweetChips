package org.sweetchips.constsweeper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public final class ConstSweeperTransformClassVisitor extends ClassVisitor {

    private final Map<String, Object> mExtra;

    private String mName;

    public ConstSweeperTransformClassVisitor(int api, ClassVisitor cv, Map<Object, Object> extra) {
        super(api, cv);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = extra == null
                ? null : (Map<String, Object>) extra.get(Util.NAME);
        mExtra = map != null ? map : Util.sConstantValues;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (mExtra.containsKey(Util.getKey(mName, name, desc))) {
            return null;
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (opcode == Opcodes.GETSTATIC) {
                    Object value = mExtra.get(Util.getKey(owner, name, desc));
                    if (value != null) {
                        super.visitLdcInsn(value);
                        return;
                    }
                }
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        };
    }
}
