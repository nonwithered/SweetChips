package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

public class UncheckcastPrepareClassVisitor extends ClassVisitor {

    private final Map<String, Map<UncheckcastRecord, UncheckcastRecord>> mExtra;

    private final Map<UncheckcastRecord, UncheckcastRecord> mTarget = new HashMap<>();

    private UncheckcastRecord mElements = null;

    private String mName;

    public UncheckcastPrepareClassVisitor(int api, ClassVisitor cv) {
        this(api, cv, null);
    }

    @SuppressWarnings("unchecked")
    public UncheckcastPrepareClassVisitor(int api, ClassVisitor cv, Map<?, ?> extra) {
        super(api, cv);
        Map<String, Map<UncheckcastRecord, UncheckcastRecord>> map = extra == null
                ? null : (Map<String, Map<UncheckcastRecord, UncheckcastRecord>>) extra.get("Uncheckcast");
        mExtra = map != null ? map : UncheckcastRecord.targets();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mElements = new UncheckcastRecord(name, superName);
        mExtra.put(mName = name, mTarget);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(UncheckcastRecord.NAME)) {
            mTarget.put(mElements, mElements);
            return new UncheckcastPrepareAnnotationVisitor(api, super.visitAnnotation(desc, visible), mElements::addType);
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        mElements = new UncheckcastRecord(name, desc);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(api, mv) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc.equals(UncheckcastRecord.NAME)) {
                    mTarget.put(mElements, mElements);
                    return new UncheckcastPrepareAnnotationVisitor(api, super.visitAnnotation(desc, visible), mElements::addType);
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }

    @Override
    public void visitEnd() {
        if (mTarget.isEmpty()) {
            mExtra.remove(mName);
        }
        super.visitEnd();
    }
}

