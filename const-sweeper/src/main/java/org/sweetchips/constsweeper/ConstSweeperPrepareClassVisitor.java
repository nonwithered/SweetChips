package org.sweetchips.constsweeper;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.sweetchips.plugin4gradle.BaseClassVisitor;
import org.sweetchips.plugin4gradle.util.ClassesUtil;

public final class ConstSweeperPrepareClassVisitor extends BaseClassVisitor {

    private boolean mIgnored;

    private String mName;

    private String[] mSupers;

    private boolean mUnused = true;

    public ConstSweeperPrepareClassVisitor(int api) {
        this(api, null);
    }

    public ConstSweeperPrepareClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mIgnored = ConstSweeperPlugin.getInstance().getExtension().isIgnored(name, null);
        mUnused = !mIgnored;
        if (!ClassesUtil.checkAccess(access, Opcodes.ACC_INTERFACE)) {
            mUnused = false;
        } else {
            mName = name;
            mSupers = interfaces;
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (mUnused) {
            if (visible) {
                mUnused = false;
            }
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        if (mUnused) {
            if (visible) {
                mUnused = false;
            }
        }
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (mUnused) {
            mUnused = false;
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (mUnused) {
            if (!Util.unusedField(access, name, desc, signature, value)) {
                mUnused = false;
            }
            if (ConstSweeperPlugin.getInstance().getExtension().isIgnored(mName, name)) {
                mUnused = false;
            }
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visitEnd() {
        if (mUnused) {
            ConstSweeperPlugin.getInstance().getExtension().unusedInterface(mName, mSupers);
        }
        super.visitEnd();
    }
}
