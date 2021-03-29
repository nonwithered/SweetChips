package org.sweetchips.inlinetailor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class InlineTailorManager {

    private final Map<String, Item> mItems = new HashMap<>();
    private final String mName;
    private final boolean mFinal;

    InlineTailorManager(String name, boolean isFinal) {
        mName = name;
        mFinal = isFinal;
    }

    void register(MethodNode mn) {
        if (!checkMethod(mn, mFinal)) {
            return;
        }
        InsnList insnList = Util.getInsnList(mn);
        if (insnList == null) {
            return;
        }
        mItems.put(Util.getItemId(mName, mn.name, mn.desc), new Item(insnList, mn.maxStack));
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
            Item item = mItems.get(Util.getItemId(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc));
            if (item == null) {
                continue;
            }
            mn.instructions.insertBefore(methodInsnNode, item.cloneInsn());
            itr.remove();
            mn.maxStack += item.mStackSize;
        }
    }

    void changeAllItems() {
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

    boolean changeOneItem(Item item) {
        boolean b = false;
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = item.mInsnList.iterator();
        while (itr.hasNext()) {
            AbstractInsnNode abstractInsnNode = itr.next();
            if (abstractInsnNode.getType() != AbstractInsnNode.METHOD_INSN) {
                continue;
            }
            MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
            Item another = mItems.get(Util.getItemId(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc));
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
                && !Util.checkAccess(mn.access, Opcodes.ACC_STATIC)
                && !Util.checkAccess(mn.access, Opcodes.ACC_FINAL)
                && !Util.checkAccess(mn.access, Opcodes.ACC_PRIVATE)
                || Util.checkAccess(mn.access, Opcodes.ACC_ABSTRACT)
                || Util.checkAccess(mn.access, Opcodes.ACC_NATIVE)
                || Util.checkAccess(mn.access, Opcodes.ACC_SYNCHRONIZED)) {
            return false;
        }
        if (mn.tryCatchBlocks.size() > 0) {
            return false;
        }
        if (mn.localVariables.size() != Util.getArgsTypes(mn.desc, Util.checkAccess(mn.access, Opcodes.ACC_STATIC)).length) {
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
                if (mItems.containsKey(Util.getItemId(invokeInsn.owner, invokeInsn.name, invokeInsn.desc))) {
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
