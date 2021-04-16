package org.sweetchips.annotationsvisitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.sweetchips.annotations.Inline;
import org.sweetchips.inlinetailor.InlineTailorHelper;
import org.sweetchips.utility.ClassesUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InlineTransformClassNode extends ClassNode {

    private final Map<String, Map.Entry<InsnList, Integer>> mItems;

    public InlineTransformClassNode(int api, Map<Object, Object> extra) {
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
            Iterator<AbstractInsnNode> itr = mn.instructions.iterator();
            while (itr.hasNext()) {
                AbstractInsnNode insn = itr.next();
                if (insn.getType() != AbstractInsnNode.METHOD_INSN) {
                    continue;
                }
                MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
                String str = ClassesUtil.toStringMethod(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
                Map.Entry<InsnList, Integer> item = mItems.get(str);
                if (item == null) {
                    continue;
                }
                InlineTailorHelper.replaceInvokeInsnList(mn.instructions, itr, methodInsnNode, item.getKey());
                mn.maxStack += item.getValue();
            }
        }
    }
}
