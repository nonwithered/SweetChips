package org.sweetchips.constsweeper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

public final class ConstSweeperTransformClassVisitor extends ClassVisitor {

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
        if (!ConstSweeperPlugin.INSTANCE.getExtension().isIgnored(mName, name)
                && Util.unusedField(access, name, desc, signature, value)) {
            return null;
        }
        return super.visitField(access, name, desc, signature, value);
    }
}
