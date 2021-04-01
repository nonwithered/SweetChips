package org.sweetchips.sourcelineeraser;

import org.objectweb.asm.ClassVisitor;

public class EraseSourceTransformClassVisitor extends ClassVisitor {

    private SourceLineEraserPlugin mPlugin;

    EraseSourceTransformClassVisitor withPlugin(SourceLineEraserPlugin plugin) {
        mPlugin = plugin;
        return this;
    }

    private String mName;

    public EraseSourceTransformClassVisitor(int i) {
        this(i, null);
    }

    public EraseSourceTransformClassVisitor(int i, ClassVisitor cv) {
        super(i, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        if (!mPlugin.getExtension().isIgnored(mName, null)) {
            return;
        }
        super.visitSource(source, debug);
    }
}
