package org.sweetchips.inlinetailor;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.sweetchips.annotations.Inline;
import org.sweetchips.platform.jvm.BaseClassNode;
import org.sweetchips.utility.ClassesUtil;
import org.sweetchips.utility.ItemsUtil;

import java.util.List;

public final class InlineTailorPlusPrepareClassNode extends BaseClassNode<InlineTailorPlusContext> {

    public InlineTailorPlusPrepareClassNode(int api) {
        super(api);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void onAccept() {
        for (MethodNode mn : (List<MethodNode>) methods) {
            if (getContext().isIgnored(name, mn.name)) {
                continue;
            }
            List<AnnotationNode> invisibleAnnotations = mn.invisibleAnnotations;
            if (invisibleAnnotations == null) {
                continue;
            }
            for (AnnotationNode an : invisibleAnnotations) {
                if (an.desc.equals("L" + Inline.class.getName().replace(".", "/") + ";")) {
                    InsnList insnList = InlineTailorHelper.tryAndGetInsnList(this, mn);
                    if (insnList != null) {
                        getContext().getItems().put(ClassesUtil.toStringMethod(name, mn.name, mn.desc), ItemsUtil.newPairEntry(insnList, mn.maxStack));
                    }
                    break;
                }
            }
        }
    }
}
