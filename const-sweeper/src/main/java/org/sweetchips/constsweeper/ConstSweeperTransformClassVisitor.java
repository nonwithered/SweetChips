package org.sweetchips.constsweeper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.sweetchips.plugin4gradle.BaseClassVisitor;

public final class ConstSweeperTransformClassVisitor extends BaseClassVisitor {

    private boolean mIgnored;

    private String mName;

    public ConstSweeperTransformClassVisitor(int api) {
        this(api, null);
    }

    public ConstSweeperTransformClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mIgnored = ConstSweeperPlugin.getInstance().getExtension().isIgnored(name, null);
        mName = name;
        interfaces = ConstSweeperPlugin.getInstance().getExtension().inheritedInterface(interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (!mIgnored
                && !ConstSweeperPlugin.getInstance().getExtension().isIgnored(mName, name)
                && Util.unusedField(access, name, desc, signature, value)) {
            return null;
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visitEnd() {
        if (ConstSweeperPlugin.getInstance().getExtension().unusedInterface(mName)) {
            setUnused();
        }
        super.visitEnd();
    }
}
