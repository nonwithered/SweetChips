package org.sweetchips.visitors;

import org.objectweb.asm.*;

import java.util.Map;

public class UncheckcastTransformClassVisitor extends ClassVisitor {

    private final Map<String, Map<UncheckcastRecord, UncheckcastRecord>> mExtra;

    private Map<UncheckcastRecord, UncheckcastRecord> mTarget;

    private UncheckcastRecord mElementClazz;

    public UncheckcastTransformClassVisitor(int api, ClassVisitor cv) {
        this(api, cv, null);
    }

    @SuppressWarnings("unchecked")
    public UncheckcastTransformClassVisitor(int api, ClassVisitor cv, Map<Object, Object> extra) {
        super(api, cv);
        Map<String, Map<UncheckcastRecord, UncheckcastRecord>> map = extra == null
                ? null : (Map<String, Map<UncheckcastRecord, UncheckcastRecord>>) extra.get("Uncheckcast");
        mExtra = map != null ? map : UncheckcastRecord.targets();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mTarget = mExtra.get(name);
        if (mTarget != null) {
            mElementClazz = mTarget.get(new UncheckcastRecord(name, superName));
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(UncheckcastRecord.NAME)) {
            return null;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        UncheckcastRecord elementMethod = null;
        if (mTarget != null) {
            elementMethod = mTarget.get(new UncheckcastRecord(name, desc));
        }
        UncheckcastRecord mElementMethod = elementMethod;
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
                if (desc.equals(UncheckcastRecord.NAME)) {
                    return null;
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }
}
