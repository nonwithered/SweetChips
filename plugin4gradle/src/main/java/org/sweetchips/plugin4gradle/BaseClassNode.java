package org.sweetchips.plugin4gradle;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.nio.file.Path;

public abstract class BaseClassNode extends ClassNode {

    protected final void setUnused() {
        Util.CLASS_UNUSED.set(true);
    }

    protected final void createClasses(Path path, byte[] bytes) {
        Util.CLASS_CREATE.get().accept(path, bytes);
    }

    private final ClassVisitor mCv;

    protected BaseClassNode(int api) {
        this(api, null);
    }

    protected BaseClassNode(int api, ClassVisitor cv) {
        super(api);
        mCv = cv;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (mCv != null) {
            accept(mCv);
        }
    }

    @Override
    public final void accept(ClassVisitor cv) {
        onAccept();
        super.accept(cv);
    }

    protected abstract void onAccept();
}
