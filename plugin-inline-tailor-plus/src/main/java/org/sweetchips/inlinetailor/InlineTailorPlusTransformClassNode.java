package org.sweetchips.inlinetailor;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.sweetchips.annotations.Inline;
import org.sweetchips.platform.jvm.BaseClassNode;
import org.sweetchips.utility.ClassesUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class InlineTailorPlusTransformClassNode extends BaseClassNode<InlineTailorPlusContext> {

    private static final String TAG = "InlineTailorPlusTransformClassNode";

    public InlineTailorPlusTransformClassNode(int api) {
        super(api);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void onAccept() {
        for (MethodNode mn : (List<MethodNode>) methods) {
            List<AnnotationNode> invisibleAnnotations = mn.invisibleAnnotations;
            if (invisibleAnnotations != null) {
                Iterator<AnnotationNode> iterator = invisibleAnnotations.iterator();
                while (iterator.hasNext()) {
                    AnnotationNode an = iterator.next();
                    if (an.desc.equals("L" + Inline.class.getName().replace(".", "/") + ";")) {
                        iterator.remove();
                        break;
                    }
                }
            }
            if (getContext().isIgnored(name, mn.name)) {
                continue;
            }
            Iterator<AbstractInsnNode> itr = mn.instructions.iterator();
            while (itr.hasNext()) {
                AbstractInsnNode insn = itr.next();
                if (insn.getType() != AbstractInsnNode.METHOD_INSN) {
                    continue;
                }
                MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
                String str = ClassesUtil.toStringMethod(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
                Map.Entry<InsnList, Integer> item = getContext().getItems().get(str);
                if (item == null) {
                    continue;
                }
                InlineTailorHelper.replaceInvokeInsnList(mn.instructions, itr, methodInsnNode, item.getKey());
                mn.maxStack += item.getValue();
                getContext().getLogger().i(TAG, ClassesUtil.toStringMethod(name, mn.name, mn.desc) + " inline invoke " + str);
            }
        }
    }
}
