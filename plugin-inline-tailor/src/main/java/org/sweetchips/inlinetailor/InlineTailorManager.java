package org.sweetchips.inlinetailor;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.sweetchips.utility.ClassesUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class InlineTailorManager {
    
    private static final String TAG = "InlineTailorManager";

    private final Map<String, InlineTailorHelper.InsnListItem> mItems = new HashMap<>();
    private final ClassNode mClassNode;
    private final InlineTailorContext mContext;

    InlineTailorManager(ClassNode cn, InlineTailorContext context) {
        mClassNode = cn;
        mContext = context;
    }

    void register(MethodNode mn) {
        InsnList insnList = InlineTailorHelper.tryAndGetInsnList(mClassNode, mn);
        if (insnList == null) {
            return;
        }
        mItems.put(ClassesUtil.toStringMethod(mClassNode.name, mn.name, mn.desc), new InlineTailorHelper.InsnListItem(mItems, insnList, mn.maxStack));
    }

    void prepare() {
        mItems.values().forEach(InlineTailorHelper.InsnListItem::prepare);
        changeAllItems();
    }

    void change(MethodNode mn) {
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = mn.instructions.iterator();
        while (itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if (insn.getType() != AbstractInsnNode.METHOD_INSN) {
                continue;
            }
            MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
            String str = ClassesUtil.toStringMethod(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
            InlineTailorHelper.InsnListItem item = mItems.get(str);
            if (item == null) {
                continue;
            }
            InlineTailorHelper.replaceInvokeInsnList(mn.instructions, itr, methodInsnNode, item.cloneInsn());
            mn.maxStack += item.mStackSize;
            mContext.getLogger().i(TAG, ClassesUtil.toStringMethod(mClassNode.name, mn.name, mn.desc) + " inline invoke " + str);
        }
    }

    private void changeAllItems() {
        while (true) {
            boolean update = false;
            for (InlineTailorHelper.InsnListItem item : mItems.values()) {
                if (item.mContains <= 0) {
                    continue;
                }
                if (changeOneItem(item)) {
                    update = true;
                }
            }
            if (!update) {
                break;
            }
        }
    }

    private boolean changeOneItem(InlineTailorHelper.InsnListItem item) {
        boolean b = false;
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = item.mInsnList.iterator();
        while (itr.hasNext()) {
            AbstractInsnNode abstractInsnNode = itr.next();
            if (abstractInsnNode.getType() != AbstractInsnNode.METHOD_INSN) {
                continue;
            }
            MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
            InlineTailorHelper.InsnListItem another = mItems.get(ClassesUtil.toStringMethod(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc));
            if (another == null || another.mContains > 0) {
                continue;
            }
            item.replaceInvoke(itr, methodInsnNode, another);
            b = true;
        }
        return b;
    }
}
