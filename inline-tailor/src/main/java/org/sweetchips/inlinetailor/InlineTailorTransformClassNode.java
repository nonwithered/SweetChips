package org.sweetchips.inlinetailor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class InlineTailorTransformClassNode extends ClassNode {

    public InlineTailorTransformClassNode(int api) {
        super(api);
    }

    @Override
    public void accept(ClassVisitor cv) {
        init();
        super.accept(cv);
    }

    private void init() {
        if (InlineTailorPlugin.INSTANCE.getExtension().isIgnored(name, null)) {
            return;
        }
        Manager manager = new Manager(this);
        manager.prepare();
        @SuppressWarnings("unchecked")
        List<MethodNode> methods = (List<MethodNode>) this.methods;
        methods.stream()
                .filter(it -> !InlineTailorPlugin.INSTANCE.getExtension().isIgnored(name, it.name))
                .forEach(manager::change);
    }

    private static boolean checkMethod(ClassNode cn, MethodNode methodNode) {
        if (InlineTailorPlugin.INSTANCE.getExtension().isIgnored(cn.name, methodNode.name)) {
            return false;
        }
        if (cn.name.equals("<init>")) {
            return false;
        }
        if (!checkAccess(cn.access, Opcodes.ACC_FINAL)
                && !checkAccess(methodNode.access, Opcodes.ACC_STATIC)
                && !checkAccess(methodNode.access, Opcodes.ACC_FINAL)
                && !checkAccess(methodNode.access, Opcodes.ACC_PRIVATE)
                || checkAccess(methodNode.access, Opcodes.ACC_ABSTRACT)
                || checkAccess(methodNode.access, Opcodes.ACC_NATIVE)
                || checkAccess(methodNode.access, Opcodes.ACC_SYNCHRONIZED)) {
            return false;
        }
        if (methodNode.tryCatchBlocks.size() > 0) {
            return false;
        }
        if (methodNode.localVariables.size() != allArgsType(methodNode).length) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) methodNode.instructions.iterator();
        int index = Integer.MAX_VALUE;
        while (itr.hasNext()) {
            AbstractInsnNode insnNode = itr.next();
            switch (insnNode.getType()) {
                case AbstractInsnNode.FRAME:
                case AbstractInsnNode.IINC_INSN:
                case AbstractInsnNode.JUMP_INSN:
                case AbstractInsnNode.TABLESWITCH_INSN:
                case AbstractInsnNode.LOOKUPSWITCH_INSN:
                case AbstractInsnNode.MULTIANEWARRAY_INSN:
                    return false;
                case AbstractInsnNode.INT_INSN:
                case AbstractInsnNode.LDC_INSN:
                    index = -1;
                    continue;
                case AbstractInsnNode.TYPE_INSN:
                    switch (insnNode.getOpcode()) {
                        case Opcodes.NEW:
                        case Opcodes.ANEWARRAY:
                            index = -1;
                    }
                    continue;
                case AbstractInsnNode.INSN:
                    if (isConstInsn(insnNode.getOpcode()) || isDupInsn(insnNode.getOpcode())) {
                        index = -1;
                    }
                    continue;
                case AbstractInsnNode.VAR_INSN:
                    if (!isLoadInsn(insnNode.getOpcode())) {
                        return false;
                    }
                    VarInsnNode varInsnNode = (VarInsnNode) insnNode;
                    if (varInsnNode.var > index) {
                        return false;
                    }
                    index = varInsnNode.var;
            }
        }
        return true;
    }

    private static InsnList insnFrom(MethodNode methodNode) {
        InsnList insnList = new InsnList();
        int[] allLoadIndex = allLoadIndex(methodNode);
        int[] allArgsType = allArgsType(methodNode);
        int topIndex = allArgsType.length - 1;
        int index = 0;
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) methodNode.instructions.iterator();
        while (itr.hasNext()) {
            AbstractInsnNode insnNode = itr.next();
            switch (insnNode.getType()) {
                case AbstractInsnNode.LABEL:
                case AbstractInsnNode.LINE:
                    continue;
                case AbstractInsnNode.VAR_INSN:
                    VarInsnNode varInsn = (VarInsnNode) insnNode;
                    while (varInsn.var < topIndex) {
                        if (allArgsType[topIndex] == Type.LONG || allArgsType[topIndex] == Type.DOUBLE) {
                            insnList.add(new InsnNode(Opcodes.POP2));
                        } else {
                            insnList.add(new InsnNode(Opcodes.POP));
                        }
                        topIndex--;
                    }
                    topIndex--;
                    if (index < allLoadIndex.length - 1 && allLoadIndex[index + 1] == varInsn.var) {
                        if (varInsn.var == Type.LONG || varInsn.var == Type.DOUBLE) {
                            insnList.add(new InsnNode(Opcodes.DUP2));
                        } else {
                            insnList.add(new InsnNode(Opcodes.DUP));
                        }
                    }
                    continue;
                case AbstractInsnNode.INSN:
                    if (isReturnInsn(insnNode.getOpcode())) {
                        switch (insnNode.getOpcode()) {
                            case Opcodes.RETURN:
                                while (-1 < topIndex) {
                                    if (allArgsType[topIndex] == Type.LONG || allArgsType[topIndex] == Type.DOUBLE) {
                                        insnList.add(new InsnNode(Opcodes.POP2));
                                    } else {
                                        insnList.add(new InsnNode(Opcodes.POP));
                                    }
                                    topIndex--;
                                }
                                continue;
                            case Opcodes.IRETURN:
                            case Opcodes.FRETURN:
                            case Opcodes.ARETURN:
                                while (-1 < topIndex) {
                                    if (allArgsType[topIndex] == Type.LONG || allArgsType[topIndex] == Type.DOUBLE) {
                                        insnList.add(new InsnNode(Opcodes.DUP_X2));
                                        insnList.add(new InsnNode(Opcodes.POP));
                                        insnList.add(new InsnNode(Opcodes.POP2));
                                    } else {
                                        insnList.add(new InsnNode(Opcodes.DUP_X1));
                                        insnList.add(new InsnNode(Opcodes.POP));
                                        insnList.add(new InsnNode(Opcodes.POP));
                                    }
                                    topIndex--;
                                }
                                continue;
                            case Opcodes.LRETURN:
                            case Opcodes.DRETURN:
                                while (-1 < topIndex) {
                                    if (allArgsType[topIndex] == Type.LONG || allArgsType[topIndex] == Type.DOUBLE) {
                                        insnList.add(new InsnNode(Opcodes.DUP2_X2));
                                        insnList.add(new InsnNode(Opcodes.POP2));
                                        insnList.add(new InsnNode(Opcodes.POP2));
                                    } else {
                                        insnList.add(new InsnNode(Opcodes.DUP2_X1));
                                        insnList.add(new InsnNode(Opcodes.POP2));
                                        insnList.add(new InsnNode(Opcodes.POP));
                                    }
                                    topIndex--;
                                }
                                continue;
                        }
                    }
                default:
                    insnList.add(insnNode.clone(null));
            }
        }
        return insnList;
    }

    private static int[] allLoadIndex(MethodNode methodNode) {
        List<Integer> indexList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) methodNode.instructions.iterator();
        itr.forEachRemaining(it -> {
            if (it.getType() == AbstractInsnNode.VAR_INSN) {
                indexList.add(((VarInsnNode) it).var);
            }
        });
        int[] allLoadIndex = new int[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            allLoadIndex[i] = indexList.get(i);
        }
        return allLoadIndex;
    }

    private static int[] allArgsType(MethodNode methodNode) {
        Type[] types = Type.getType(methodNode.desc).getArgumentTypes();
        boolean self = !checkAccess(methodNode.access, Opcodes.ACC_STATIC);
        int[] allArgsType = new int[types.length + (self ? 1 : 0)];
        int index = 0;
        if (self) {
            allArgsType[index++] = -1;
        }
        for (Type type : types) {
            allArgsType[index++] = type.getSort();
        }
        return allArgsType;
    }

    private static int addStackSize(MethodNode methodNode) {
        int[] allArgsType = allArgsType(methodNode);
        int[] allLoadIndex = allLoadIndex(methodNode);
        int addStackSize = 0;
        for (int i = 0; i < allLoadIndex.length; i++) {
            int loadIndex = allLoadIndex[i];
            if (i > 0 && loadIndex == allLoadIndex[i - 1]) {
                if (allArgsType[loadIndex] == Opcodes.LONG || allArgsType[loadIndex] == Opcodes.DOUBLE) {
                    addStackSize = 2;
                    break;
                } else if (addStackSize == 0) {
                    addStackSize = 1;
                }
            }
        }
        return addStackSize;
    }

    private static boolean checkAccess(int access, int flag) {
        return (access & flag) != 0;
    }

    private static String getItemId(String cls, String mtd, String desc) {
        return cls + "->" + mtd + desc;
    }

    private static boolean isLoadInsn(int opcode) {
        return opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD;
    }

    private static boolean isReturnInsn(int opcode) {
        return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
    }

    private static boolean isConstInsn(int opcode) {
        return opcode >= Opcodes.ACONST_NULL && opcode <= Opcodes.DCONST_1;
    }

    private static boolean isDupInsn(int opcode) {
        return opcode >= Opcodes.DUP && opcode <= Opcodes.DUP2_X2;
    }

    private static class Manager {

        private final Map<String, Item> items;

        Manager(ClassNode cn) {
            items = new HashMap<>();
            @SuppressWarnings("unchecked")
            List<MethodNode> methodNodes = (List<MethodNode>) cn.methods;
            methodNodes.stream().filter(it -> InlineTailorTransformClassNode.checkMethod(cn, it)).forEach(it ->
                    items.put(getItemId(cn.name, it.name, it.desc), new Item(it)));
        }

        void prepare() {
            items.values().forEach(Item::prepare);
            changeAllItems();
        }

        void change(MethodNode method) {
            @SuppressWarnings("unchecked")
            Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) method.instructions.iterator();
            while (itr.hasNext()) {
                AbstractInsnNode insn = itr.next();
                if (insn.getType() != AbstractInsnNode.METHOD_INSN) {
                    continue;
                }
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                Item item = items.get(getItemId(methodInsn.owner, methodInsn.name, methodInsn.desc));
                if (item == null) {
                    continue;
                }
                method.instructions.insertBefore(methodInsn, item.cloneInsn());
                itr.remove();
                method.maxStack += item.stackSize;
            }
        }

        void changeAllItems() {
            while (true) {
                boolean update = false;
                for (Item item : items.values()) {
                    if (item.contains <= 0) {
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
            Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) item.insnList.iterator();
            while (itr.hasNext()) {
                AbstractInsnNode insn = itr.next();
                if (insn.getType() != AbstractInsnNode.METHOD_INSN) {
                    continue;
                }
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                Item another = items.get(getItemId(methodInsn.owner, methodInsn.name, methodInsn.desc));
                if (another == null || another.contains > 0) {
                    continue;
                }
                item.replaceInvoke(itr, methodInsn, another);
                b = true;
            }
            return b;
        }

        private class Item {

            final InsnList insnList;
            int stackSize;
            int contains;

            Item(MethodNode methodNode) {
                insnList = insnFrom(methodNode);
                stackSize = methodNode.maxStack + addStackSize(methodNode);
            }

            void prepare() {
                if (contains != 0) {
                    throw new IllegalStateException();
                }
                @SuppressWarnings("unchecked")
                Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) insnList.iterator();
                while (itr.hasNext()) {
                    AbstractInsnNode insn = itr.next();
                    if (insn.getType() != AbstractInsnNode.METHOD_INSN) {
                        continue;
                    }
                    MethodInsnNode invokeInsn = (MethodInsnNode) insn;
                    if (items.containsKey(getItemId(invokeInsn.owner, invokeInsn.name, invokeInsn.desc))) {
                        contains++;
                    }
                }
            }

            InsnList cloneInsn() {
                InsnList clone = new InsnList();
                Map<LabelNode, LabelNode> labels = new HashMap<>();
                @SuppressWarnings("unchecked")
                Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) insnList.iterator();
                itr.forEachRemaining(it -> {
                    if (it.getType() == AbstractInsnNode.LABEL) {
                        labels.put((LabelNode) it, new LabelNode());
                    }
                    clone.add(it.clone(labels));
                });
                return clone;
            }

            void replaceInvoke(Iterator<AbstractInsnNode> itr, MethodInsnNode methodInsn, Item item) {
                insnList.insertBefore(methodInsn, item.cloneInsn());
                itr.remove();
                stackSize += item.stackSize;
                contains--;
            }
        }
    }
}