package org.sweetchips.annotationsvisitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.sweetchips.annotations.Inline;
import org.sweetchips.inlinetailor.InlineTailorHelper;
import org.sweetchips.utility.ClassesUtil;
import org.sweetchips.utility.ItemsUtil;

import java.util.List;
import java.util.Map;

public class InlinePrepareClassNode extends ClassNode {

    private final Map<String, Map.Entry<InsnList, Integer>> mItems;

    public InlinePrepareClassNode(int api, Map<Object, Object> extra) {
        super(api);
        Object obj = extra.get(Inline.class.getName());
        @SuppressWarnings("unchecked")
        Map<String, Map.Entry<InsnList, Integer>> items = obj instanceof Map
                ? (Map<String, Map.Entry<InsnList, Integer>>) obj
                : null;
        mItems = items;
    }

    @Override
    public final void accept(ClassVisitor cv) {
        onAccept();
        super.accept(cv);
    }

    @SuppressWarnings("unchecked")
    private void onAccept() {
        if (mItems == null) {
            return;
        }
        for (MethodNode mn : (List<MethodNode>) methods) {
            List<AnnotationNode> invisibleAnnotations = mn.invisibleAnnotations;
            if (invisibleAnnotations == null) {
                continue;
            }
            for (AnnotationNode an : invisibleAnnotations) {
                if (an.desc.equals("L" + Inline.class.getName().replace(".", "/") + ";")) {
                    InsnList insnList = InlineTailorHelper.tryAndGetInsnList(this, mn);
                    if (insnList != null) {
                        mItems.put(ClassesUtil.toStringMethod(name, mn.name, mn.desc), ItemsUtil.newPairEntry(insnList, mn.maxStack));
                    }
                    break;
                }
            }
        }
    }
}
