package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HidePrepareClassVisitor extends ClassVisitor {

    private final Map<String, Set<HideRecord>> mExtra;

    private final Set<HideRecord> mTarget = new HashSet<>();

    private HideRecord mElements = null;

    private String mName;

    public HidePrepareClassVisitor(int api, ClassVisitor cv) {
        this(api, cv, null);
    }

    @SuppressWarnings("unchecked")
    public HidePrepareClassVisitor(int api, ClassVisitor cv, Map<?, ?> extra) {
        super(api, cv);
        Map<String, Set<HideRecord>> map = extra == null
                ? null : (Map<String, Set<HideRecord>>) extra.get("Hide");
        mExtra = map != null ? map : HideRecord.targets();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mElements = new HideRecord(name, superName);
        mExtra.put(mName = name, mTarget);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(HideRecord.NAME)) {
            mTarget.add(mElements);
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        mElements = new HideRecord(name, desc);
        return new FieldVisitor(api, super.visitField(access, name, desc, signature, value)) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc.equals(HideRecord.NAME)) {
                    mTarget.add(mElements);
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        mElements = new HideRecord(name, desc);
        return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc.equals(HideRecord.NAME)) {
                    mTarget.add(mElements);
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

