package org.sweetchips.visitors;

import org.objectweb.asm.*;

import java.util.Map;

public class UncheckcastDumpClassVisitor extends ClassVisitor {

    private Map<UncheckcastElement, UncheckcastElement> mTarget;

    private UncheckcastElement mElementClazz;

    public UncheckcastDumpClassVisitor(ClassVisitor cv) {
        super(Util.ASM_API.get(), cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mTarget = Util.UNCHECKCAST_TARGET.get(name);
        if (mTarget != null) {
            mElementClazz = mTarget.get(new UncheckcastElement(name, superName));
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(Util.UNCHECKCAST_NAME)) {
            return null;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        UncheckcastElement elementMethod = null;
        if (mTarget != null) {
            elementMethod = mTarget.get(new UncheckcastElement(name, desc));
        }
        UncheckcastElement mElementMethod = elementMethod;
        return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
            @Override
            public void visitTypeInsn(int opcode, String type) {
                if (opcode == Opcodes.CHECKCAST) {
                    String s = type;
                    if (type.charAt(0) != '[') {
                        s = 'L' + s + ';';
                    }
                    Type t = Type.getType(s);
                    if (mElementClazz != null && (mElementClazz.isEmptyTypes() || mElementClazz.containsType(t))
                            || mElementMethod != null && (mElementMethod.isEmptyTypes() || mElementMethod.containsType(t))) {
                        return;
                    }
                }
                super.visitTypeInsn(opcode, type);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc.equals(Util.UNCHECKCAST_NAME)) {
                    return null;
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }
}
