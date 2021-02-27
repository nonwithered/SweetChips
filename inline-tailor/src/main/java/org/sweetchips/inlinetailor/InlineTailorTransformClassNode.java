package org.sweetchips.inlinetailor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.sweetchips.plugin4gradle.BaseClassNode;
import org.sweetchips.plugin4gradle.util.ClassesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class InlineTailorTransformClassNode extends BaseClassNode {

    public InlineTailorTransformClassNode(int api) {
        this(api, null);
    }

    public InlineTailorTransformClassNode(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    protected void onAccept() {
        if (InlineTailorPlugin.getInstance().getExtension().isIgnored(name, null)) {
            return;
        }
        Manager manager = createManager();
        @SuppressWarnings("unchecked")
        List<MethodNode> methods = (List<MethodNode>) this.methods;
        methods.stream()
                .filter(it -> !InlineTailorPlugin.getInstance().getExtension().isIgnored(name, it.name))
                .forEach(manager::change);
    }

    private boolean checkMethod(MethodNode methodNode) {
        if (InlineTailorPlugin.getInstance().getExtension().isIgnored(name, methodNode.name)) {
            return false;
        }
        if (!ClassesUtil.checkAccess(access, Opcodes.ACC_FINAL)
                && !ClassesUtil.checkAccess(methodNode.access, Opcodes.ACC_FINAL)
                && !ClassesUtil.checkAccess(methodNode.access, Opcodes.ACC_PRIVATE)
                || ClassesUtil.checkAccess(methodNode.access, Opcodes.ACC_ABSTRACT)
                || ClassesUtil.checkAccess(methodNode.access, Opcodes.ACC_NATIVE)) {
            return false;
        }
        if (methodNode.tryCatchBlocks.size() > 0) {
            return false;
        }
        if (methodNode.maxLocals != methodNode.localVariables.size()) {
            return false;
        }
        if (methodNode.maxLocals != allArgsType(methodNode).length) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) methodNode.instructions.iterator();
        int index = Integer.MAX_VALUE;
        while (itr.hasNext()) {
            AbstractInsnNode insnNode = itr.next();
            if (insnNode instanceof JumpInsnNode
                    || insnNode instanceof TableSwitchInsnNode
                    || insnNode instanceof LookupSwitchInsnNode) {
                return false;
            }
            if (insnNode instanceof VarInsnNode) {
                VarInsnNode varInsnNode = (VarInsnNode) insnNode;
                if (!isLoadInsn(varInsnNode.getOpcode()) || varInsnNode.var > index) {
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
            AbstractInsnNode insn = itr.next();
            if (insn instanceof VarInsnNode) {
                VarInsnNode varInsn = (VarInsnNode) insn;
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
            } else if (!(insn instanceof LabelNode) && !(insn instanceof LineNumberNode)) {
                insnList.add(insn.clone(null));
            }
        }
        while (topIndex >= 0) {
            if (allArgsType[topIndex] == Type.LONG || allArgsType[topIndex] == Type.DOUBLE) {
                insnList.add(new InsnNode(Opcodes.POP2));
            } else {
                insnList.add(new InsnNode(Opcodes.POP));
            }
            topIndex--;
        }
        return insnList;
    }

    private static int[] allLoadIndex(MethodNode methodNode) {
        List<Integer> indexList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) methodNode.instructions.iterator();
        itr.forEachRemaining(it -> {
            if (it instanceof VarInsnNode) {
                indexList.add(((VarInsnNode) it).var);
            }
        });
        int[] allLoadIndex = new int[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            allLoadIndex[i] = indexList.get(i);
        }
        return allLoadIndex;
    }

    private static boolean isLoadInsn(int opcode) {
        return opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD;
    }

    private static int[] allArgsType(MethodNode methodNode) {
        Type[] types = Type.getType(methodNode.desc).getArgumentTypes();
        boolean self = !ClassesUtil.checkAccess(methodNode.access, Opcodes.ACC_STATIC);
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

    private static String getItemId(String cls, String mtd, String desc) {
        return cls + "#" + mtd + desc;
    }

    private Manager createManager() {
        Manager manager = new Manager();
        manager.prepare();
        return manager;
    }

    private class Manager {

        private final Map<String, Item> items;

        Manager() {
            items = new HashMap<>();
            @SuppressWarnings("unchecked")
            List<MethodNode> methodNodes = (List<MethodNode>) methods;
            methodNodes.stream().filter(InlineTailorTransformClassNode.this::checkMethod).forEach(it ->
                    items.put(getItemId(name, it.name, it.desc), new Item(it)));
        }

        void change(MethodNode method) {
            @SuppressWarnings("unchecked")
            Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) method.instructions.iterator();
            while (itr.hasNext()) {
                AbstractInsnNode insn = itr.next();
                if (!(insn instanceof MethodInsnNode)) {
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

        void prepare() {
            items.values().forEach(Item::prepare);
            changeAllItems();
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
                if (!(insn instanceof MethodInsnNode)) {
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
                    if (!(insn instanceof MethodInsnNode)) {
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
                @SuppressWarnings("unchecked")
                Iterator<AbstractInsnNode> itr = (Iterator<AbstractInsnNode>) insnList.iterator();
                itr.forEachRemaining(it -> {
                    if (it instanceof LabelNode || it instanceof LineNumberNode) {
                        return;
                    }
                    clone.add(it.clone(null));
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