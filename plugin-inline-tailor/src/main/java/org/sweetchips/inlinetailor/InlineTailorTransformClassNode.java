package org.sweetchips.inlinetailor;

import org.objectweb.asm.tree.MethodNode;
import org.sweetchips.platform.jvm.BaseClassNode;

import java.util.List;

public final class InlineTailorTransformClassNode extends BaseClassNode<InlineTailorContext> {
    
    public InlineTailorTransformClassNode(int api) {
        super(api);
    }

    @Override
    protected final void onAccept() {
        if (getContext().isIgnored(name, null)) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<MethodNode> methods = this.methods;
        InlineTailorManager manager = new InlineTailorManager(this, getContext());
        methods.stream().filter(it ->
                !getContext().isIgnored(name, it.name)
                        && !it.name.equals("<init>") && !it.name.equals("<clinit>")
        ).forEach(manager::register);
        manager.prepare();
        methods.stream().filter(it ->
                !getContext().isIgnored(name, it.name)
        ).forEach(manager::change);
    }
}