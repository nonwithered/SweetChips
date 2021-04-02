package org.sweetchips.recursivetail;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.sweetchips.platform.jvm.BaseClassNode;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class RecursiveTailClassNode extends BaseClassNode<RecursiveTailContext> {

    public RecursiveTailClassNode(int api) {
        super(api);
    }

    @Override
    public void accept(ClassVisitor cv) {
        onAccept();
        super.accept(cv);
    }

    private void onAccept() {
        @SuppressWarnings("unchecked")
        List<MethodNode> methods = this.methods;
        methods.stream()
                .filter(this::check)
                .forEach(this::change);
    }

    private boolean check(MethodNode mn) {
        if (mn.name.equals("<init>") || mn.name.equals("<clinit>")) {
            return false;
        }
        if (getContext().isIgnored(name, mn.name)) {
            return false;
        }
        if (!checkAccess(access, Opcodes.ACC_FINAL)
                && !checkAccess(mn.access, Opcodes.ACC_STATIC)
                && !checkAccess(mn.access, Opcodes.ACC_FINAL)
                && !checkAccess(mn.access, Opcodes.ACC_PRIVATE)
                || checkAccess(mn.access, Opcodes.ACC_ABSTRACT)
                || checkAccess(mn.access, Opcodes.ACC_NATIVE)) {
            return false;
        }
        return true;
    }

    private void change(MethodNode methodNode) {
        InsnList insnList = null;
        LabelNode labelNode = null;
        @SuppressWarnings("unchecked")
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode abstractInsnNode = iterator.next();
            switch (abstractInsnNode.getType()) {
                case AbstractInsnNode.LABEL:
                    if (labelNode == null) {
                        labelNode = (LabelNode) abstractInsnNode;
                    }
                    break;
                case AbstractInsnNode.INSN:
                    InsnNode insnNode = (InsnNode) abstractInsnNode;
                    if (insnNode.getOpcode() < Opcodes.IRETURN || Opcodes.RETURN < insnNode.getOpcode()) {
                        break;
                    }
                    abstractInsnNode = insnNode.getPrevious();
                    if (abstractInsnNode.getType() != AbstractInsnNode.METHOD_INSN) {
                        break;
                    }
                    MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
                    if (!methodInsnNode.owner.equals(name)
                            || !methodInsnNode.name.equals(methodNode.name)
                            || !methodInsnNode.desc.equals(methodNode.desc)) {
                        break;
                    }
                    if (labelNode == null) {
                        break;
                    }
                    if (insnList == null) {
                        insnList = getInsns(methodInsnNode);
                        methodNode.instructions.insert(labelNode, new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                    }
                    methodNode.instructions.insertBefore(abstractInsnNode, cloneInsnList(insnList));
                    methodNode.instructions.insertBefore(abstractInsnNode, new JumpInsnNode(Opcodes.GOTO, labelNode));
                    iterator.remove();
                    methodNode.instructions.remove(methodInsnNode);
                    break;
            }
        }
    }

    private static InsnList cloneInsnList(InsnList insnList) {
        InsnList clone = new InsnList();
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> iterator = insnList.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode abstractInsnNode = iterator.next();
            clone.add(abstractInsnNode.clone(null));
        }
        return clone;
    }

    private static InsnList getInsns(MethodInsnNode methodInsnNode) {
        InsnList insnList = new InsnList();
        int[] argsTypes = getArgsTypes(methodInsnNode.desc, methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC);
        for (int i = argsTypes.length - 1; i >= 0; i--) {
            switch (argsTypes[i]) {
                case Type.BOOLEAN:
                case Type.CHAR:
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                    insnList.add(new VarInsnNode(Opcodes.ISTORE, i));
                    break;
                case Type.FLOAT:
                    insnList.add(new VarInsnNode(Opcodes.FSTORE, i));
                    break;
                case Type.LONG:
                    insnList.add(new VarInsnNode(Opcodes.LSTORE, i));
                    break;
                case Type.DOUBLE:
                    insnList.add(new VarInsnNode(Opcodes.DSTORE, i));
                    break;
                default:
                    insnList.add(new VarInsnNode(Opcodes.ASTORE, i));
            }
        }
        return insnList;
    }

    private static int[] getArgsTypes(String desc, boolean isStatic) {
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

    private static boolean checkAccess(int access, int flag) {
        return (access & flag) != 0;
    }
}
