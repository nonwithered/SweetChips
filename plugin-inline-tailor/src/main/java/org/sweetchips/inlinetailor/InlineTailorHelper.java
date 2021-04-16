package org.sweetchips.inlinetailor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.sweetchips.utility.ClassesUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class InlineTailorHelper {

    private InlineTailorHelper() {
        throw new UnsupportedOperationException();
    }

    public static InsnList tryAndGetInsnList(ClassNode cn, MethodNode mn) {
        if (!InlineTailorHelper.checkMethod(mn, InlineTailorHelper.checkAccess(cn.access, Opcodes.ACC_FINAL))) {
            return null;
        }
        return InlineTailorHelper.getInsnList(mn);
    }

    public static void replaceInvokeInsnList(InsnList instructions, Iterator<AbstractInsnNode> itr, MethodInsnNode methodInsnNode, InsnList insnList) {
        instructions.insertBefore(methodInsnNode, insnList);
        itr.remove();
    }

    static int[] getArgsTypes(String desc, boolean isStatic) {
        Type[] types = Type.getType(desc).getArgumentTypes();
        int[] argsTypes = new int[types.length + (isStatic ? 0 : 1)];
        int index = 0;
        if (!isStatic) {
            argsTypes[index++] = Type.OBJECT;
        }
        for (Type type : types) {
            argsTypes[index++] = type.getSort();
        }
        return argsTypes;
    }

    private static boolean checkMethod(MethodNode mn, boolean isFinal) {
        if (!isFinal
                && !InlineTailorHelper.checkAccess(mn.access, Opcodes.ACC_STATIC)
                && !InlineTailorHelper.checkAccess(mn.access, Opcodes.ACC_FINAL)
                && !InlineTailorHelper.checkAccess(mn.access, Opcodes.ACC_PRIVATE)
                || InlineTailorHelper.checkAccess(mn.access, Opcodes.ACC_ABSTRACT)
                || InlineTailorHelper.checkAccess(mn.access, Opcodes.ACC_NATIVE)
                || InlineTailorHelper.checkAccess(mn.access, Opcodes.ACC_SYNCHRONIZED)) {
            return false;
        }
        if (mn.tryCatchBlocks.size() > 0) {
            return false;
        }
        if (mn.localVariables.size() != InlineTailorHelper.getArgsTypes(mn.desc, InlineTailorHelper.checkAccess(mn.access, Opcodes.ACC_STATIC)).length) {
            return false;
        }
        return true;
    }

    private static boolean checkAccess(int access, int flag) {
        return (access & flag) != 0;
    }

    private static InsnList getInsnList(MethodNode mn) {
        Map<LabelNode, LabelNode> labels = new HashMap<>();
        InlineTailorStackMirror stack = new InlineTailorStackMirror(mn.maxStack, getArgsTypes(mn.desc, checkAccess(mn.access, Opcodes.ACC_STATIC)));
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = mn.instructions.iterator();
        while (itr.hasNext()) {
            AbstractInsnNode abstractInsnNode = itr.next();
            switch (abstractInsnNode.getType()) {
                case AbstractInsnNode.INSN:
                    if (stack.addInsnNode((InsnNode) abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.INT_INSN:
                    if (stack.addIntInsnNode((IntInsnNode) abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.VAR_INSN:
                    if (stack.addVarInsnNode((VarInsnNode) abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.TYPE_INSN:
                    if (stack.addTypeInsnNode((TypeInsnNode) abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.FIELD_INSN:
                    if (stack.addFieldInsnNode((FieldInsnNode) abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.METHOD_INSN:
                    if (stack.addMethodInsnNode((MethodInsnNode) abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                    if (stack.addInvokeDynamicInsnNode((InvokeDynamicInsnNode) abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.LDC_INSN:
                    if (stack.addLdcInsnNode((LdcInsnNode) abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.MULTIANEWARRAY_INSN:
                    if (stack.addMultiANewArrayInsnNode((MultiANewArrayInsnNode) abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.LABEL:
                    labels.put((LabelNode) abstractInsnNode, new LabelNode());
                case AbstractInsnNode.LINE:
                    if (stack.addAbstractInsnNode(abstractInsnNode.clone(labels))) {
                        continue;
                    } else {
                        return null;
                    }
                case AbstractInsnNode.JUMP_INSN:
                case AbstractInsnNode.IINC_INSN:
                case AbstractInsnNode.TABLESWITCH_INSN:
                case AbstractInsnNode.LOOKUPSWITCH_INSN:
                case AbstractInsnNode.FRAME:
                    return null;
            }
        }
        return stack.getInsnList();
    }

    static class InsnListItem {

        final Map<String, InsnListItem> mItems;
        final InsnList mInsnList;
        int mStackSize;
        int mContains;

        InsnListItem(Map<String, InsnListItem> items, InsnList insnList, int stackSize) {
            mItems = items;
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

        void replaceInvoke(Iterator<AbstractInsnNode> itr, MethodInsnNode methodInsn, InsnListItem item) {
            replaceInvokeInsnList(mInsnList, itr, methodInsn, item.cloneInsn());
            mStackSize += item.mStackSize;
            mContains--;
        }
    }
}
