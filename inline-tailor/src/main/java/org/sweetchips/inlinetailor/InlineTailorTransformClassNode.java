package org.sweetchips.inlinetailor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public final class InlineTailorTransformClassNode extends ClassNode {

    public InlineTailorTransformClassNode(int api) {
        super(api);
    }

    @Override
    public void accept(ClassVisitor cv) {
        onAccept();
        super.accept(cv);
    }

    private void onAccept() {
        if (InlineTailorPlugin.INSTANCE.getExtension().isIgnored(name, null)) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<MethodNode> methods = this.methods;
        InlineTailorManager manager = new InlineTailorManager(name, Util.checkAccess(access, Opcodes.ACC_FINAL));
        methods.stream().filter(it ->
                !InlineTailorPlugin.INSTANCE.getExtension().isIgnored(name, it.name)
                        && !it.name.equals("<init>") && !it.name.equals("<clinit>")
        ).forEach(manager::register);
        manager.prepare();
        methods.stream().filter(it ->
                !InlineTailorPlugin.INSTANCE.getExtension().isIgnored(name, it.name)
        ).forEach(manager::change);
    }
}