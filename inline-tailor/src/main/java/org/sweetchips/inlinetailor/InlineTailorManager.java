package org.sweetchips.inlinetailor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.sweetchips.utility.ClassesUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class InlineTailorManager {
    
    private static final String TAG = "InlineTailorManager";

    private final Map<String, Item> mItems = new HashMap<>();
    private final ClassNode mClassNode;
    private final InlineTailorContext mContext;

    InlineTailorManager(ClassNode cn, InlineTailorContext context) {
        mClassNode = cn;
        mContext = context;
    }

    void register(MethodNode mn) {
        if (!checkMethod(mn, InlineTailorContext.checkAccess(mClassNode.access, Opcodes.ACC_FINAL))) {
            return;
        }
        InsnList insnList = InlineTailorContext.getInsnList(mn);
        if (insnList == null) {
            return;
        }
        mItems.put(ClassesUtil.toStringMethod(mClassNode.name, mn.name, mn.desc), new Item(insnList, mn.maxStack));
    }

    void prepare() {
        mItems.values().forEach(Item::prepare);
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
            Item item = mItems.get(str);
            if (item == null) {
                continue;
            }
            mn.instructions.insertBefore(methodInsnNode, item.cloneInsn());
            itr.remove();
            mn.maxStack += item.mStackSize;
            mContext.getLogger().i(TAG, ClassesUtil.toStringMethod(mClassNode.name, mn.name, mn.desc) + " inline invoke " + str);
        }
    }

    private void changeAllItems() {
        while (true) {
            boolean update = false;
            for (Item item : mItems.values()) {
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

    private boolean changeOneItem(Item item) {
        boolean b = false;
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = item.mInsnList.iterator();
        while (itr.hasNext()) {
            AbstractInsnNode abstractInsnNode = itr.next();
            if (abstractInsnNode.getType() != AbstractInsnNode.METHOD_INSN) {
                continue;
            }
            MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
            Item another = mItems.get(ClassesUtil.toStringMethod(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc));
            if (another == null || another.mContains > 0) {
                continue;
            }
            item.replaceInvoke(itr, methodInsnNode, another);
            b = true;
        }
        return b;
    }

    private static boolean checkMethod(MethodNode mn, boolean isFinal) {
        if (!isFinal
                && !InlineTailorContext.checkAccess(mn.access, Opcodes.ACC_STATIC)
                && !InlineTailorContext.checkAccess(mn.access, Opcodes.ACC_FINAL)
                && !InlineTailorContext.checkAccess(mn.access, Opcodes.ACC_PRIVATE)
                || InlineTailorContext.checkAccess(mn.access, Opcodes.ACC_ABSTRACT)
                || InlineTailorContext.checkAccess(mn.access, Opcodes.ACC_NATIVE)
                || InlineTailorContext.checkAccess(mn.access, Opcodes.ACC_SYNCHRONIZED)) {
            return false;
        }
        if (mn.tryCatchBlocks.size() > 0) {
            return false;
        }
        if (mn.localVariables.size() != InlineTailorContext.getArgsTypes(mn.desc, InlineTailorContext.checkAccess(mn.access, Opcodes.ACC_STATIC)).length) {
            return false;
        }
        return true;
    }

    private class Item {

        final InsnList mInsnList;
        int mStackSize;
        int mContains;

        Item(InsnList insnList, int stackSize) {
            mInsnList = insnList;
            mStackSize = stackSize;
        }

        void prepare() {
            if (mContains != 0) {
                throw new IllegalStateException();
            }
            @SuppressWarnings("unchecked")
            Iterator<AbstractInsnNode> itr = mInsnList.iterator();
            while (itr.hasNext()) {
                AbstractInsnNode insn = itr.next();
                if (insn.getType() != AbstractInsnNode.METHOD_INSN) {
                    continue;
                }
                MethodInsnNode invokeInsn = (MethodInsnNode) insn;
                if (mItems.containsKey(ClassesUtil.toStringMethod(invokeInsn.owner, invokeInsn.name, invokeInsn.desc))) {
                    mContains++;
                }
            }
        }

        InsnList cloneInsn() {
            InsnList clone = new InsnList();
            Map<LabelNode, LabelNode> labels = new HashMap<>();
            @SuppressWarnings("unchecked")
            Iterator<AbstractInsnNode> itr = mInsnList.iterator();
            itr.forEachRemaining(it -> {
                if (it.getType() == AbstractInsnNode.LABEL) {
                    labels.put((LabelNode) it, new LabelNode());
                }
                clone.add(it.clone(labels));
            });
            return clone;
        }

        void replaceInvoke(Iterator<AbstractInsnNode> itr, MethodInsnNode methodInsn, Item item) {
            mInsnList.insertBefore(methodInsn, item.cloneInsn());
            itr.remove();
            mStackSize += item.mStackSize;
            mContains--;
        }
    }
}
