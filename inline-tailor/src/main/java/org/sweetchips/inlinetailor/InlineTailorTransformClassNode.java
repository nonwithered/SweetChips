package org.sweetchips.inlinetailor;

import org.objectweb.asm.ClassVisitor;
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
        onAccept();
        super.accept(cv);
    }

    private void onAccept() {
        if (InlineTailorPlugin.INSTANCE.getExtension().isIgnored(name, null)) {
            return;
        }
        Manager manager = new Manager(this);
        manager.prepare();
        @SuppressWarnings("unchecked")
        List<MethodNode> methods = this.methods;
        methods.stream()
                .filter(it -> !InlineTailorPlugin.INSTANCE.getExtension().isIgnored(name, it.name))
                .forEach(manager::change);
    }

    private static boolean checkMethod(ClassNode cn, MethodNode mn) {
        if (cn.name.equals("<init>") || cn.name.equals("<clinit>")) {
            return false;
        }
        if (InlineTailorPlugin.INSTANCE.getExtension().isIgnored(cn.name, mn.name)) {
            return false;
        }
        if (!checkAccess(cn.access, Opcodes.ACC_FINAL)
                && !checkAccess(mn.access, Opcodes.ACC_STATIC)
                && !checkAccess(mn.access, Opcodes.ACC_FINAL)
                && !checkAccess(mn.access, Opcodes.ACC_PRIVATE)
                || checkAccess(mn.access, Opcodes.ACC_ABSTRACT)
                || checkAccess(mn.access, Opcodes.ACC_NATIVE)
                || checkAccess(mn.access, Opcodes.ACC_SYNCHRONIZED)) {
            return false;
        }
        if (mn.tryCatchBlocks.size() > 0) {
            return false;
        }
        if (mn.localVariables.size() != getArgsTypes(mn.desc, checkAccess(mn.access, Opcodes.ACC_STATIC)).length) {
            return false;
        }
        return true;
    }

    private static InsnList getInsnList(MethodNode mn) {
        InsnList insnList = new InsnList();
        Map<LabelNode, LabelNode> labels = new HashMap<>();
        int[] argsTypes = getArgsTypes(mn.desc, checkAccess(mn.access, Opcodes.ACC_STATIC));
        int maxVar = argsTypes.length - 1;
        int nextVar = maxVar;
        int[] stack = new int[mn.maxStack];
        int top = -1;
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> itr = mn.instructions.iterator();
        while (itr.hasNext()) {
            AbstractInsnNode abstractInsnNode = itr.next();
            switch (abstractInsnNode.getType()) {
                case AbstractInsnNode.INSN:
                    InsnNode insnNode = (InsnNode) abstractInsnNode;
                    switch (insnNode.getOpcode()) {
                        case Opcodes.NOP:
                            break;
                        case Opcodes.ACONST_NULL:
                        case Opcodes.ICONST_M1:
                        case Opcodes.ICONST_0:
                        case Opcodes.ICONST_1:
                        case Opcodes.ICONST_2:
                        case Opcodes.ICONST_3:
                        case Opcodes.ICONST_4:
                        case Opcodes.ICONST_5:
                        case Opcodes.FCONST_0:
                        case Opcodes.FCONST_1:
                        case Opcodes.FCONST_2:
                            stack[++top] = -1;
                            break;
                        case Opcodes.LCONST_0:
                        case Opcodes.LCONST_1:
                        case Opcodes.DCONST_0:
                        case Opcodes.DCONST_1:
                            stack[++top] = -2;
                            break;
                        case Opcodes.IALOAD:
                        case Opcodes.FALOAD:
                        case Opcodes.AALOAD:
                        case Opcodes.BALOAD:
                        case Opcodes.CALOAD:
                        case Opcodes.SALOAD:
                        case Opcodes.IADD:
                        case Opcodes.FADD:
                        case Opcodes.ISUB:
                        case Opcodes.FSUB:
                        case Opcodes.IMUL:
                        case Opcodes.FMUL:
                        case Opcodes.IDIV:
                        case Opcodes.FDIV:
                        case Opcodes.IREM:
                        case Opcodes.FREM:
                        case Opcodes.ISHL:
                        case Opcodes.ISHR:
                        case Opcodes.IUSHR:
                        case Opcodes.IAND:
                        case Opcodes.IOR:
                        case Opcodes.IXOR:
                        case Opcodes.LCMP:
                        case Opcodes.FCMPL:
                        case Opcodes.FCMPG:
                        case Opcodes.DCMPL:
                        case Opcodes.DCMPG:
                            stack[--top] = -1;
                            break;
                        case Opcodes.LALOAD:
                        case Opcodes.DALOAD:
                        case Opcodes.LADD:
                        case Opcodes.DADD:
                        case Opcodes.LSUB:
                        case Opcodes.DSUB:
                        case Opcodes.LMUL:
                        case Opcodes.DMUL:
                        case Opcodes.LDIV:
                        case Opcodes.DDIV:
                        case Opcodes.LREM:
                        case Opcodes.DREM:
                        case Opcodes.LSHL:
                        case Opcodes.LSHR:
                        case Opcodes.LUSHR:
                        case Opcodes.LAND:
                        case Opcodes.LOR:
                        case Opcodes.LXOR:
                            stack[--top] = -2;
                            break;
                        case Opcodes.IASTORE:
                        case Opcodes.LASTORE:
                        case Opcodes.FASTORE:
                        case Opcodes.DASTORE:
                        case Opcodes.AASTORE:
                        case Opcodes.BASTORE:
                        case Opcodes.CASTORE:
                        case Opcodes.SASTORE:
                            top -= 3;
                            break;
                        case Opcodes.MONITORENTER:
                        case Opcodes.MONITOREXIT:
                        case Opcodes.POP:
                        case Opcodes.POP2:
                            top--;
                            break;
                        case Opcodes.DUP:
                            if (stack[top] == -2 || stack[top] >= 0
                                    && argsTypes[stack[top]] == Type.LONG
                                    && argsTypes[stack[top]] == Type.DOUBLE) {
                                return null;
                            }
                            stack[top + 1] = stack[top];
                            top++;
                            break;
                        case Opcodes.DUP_X1:
                            if (stack[top] == -2 || stack[top] >= 0
                                    && argsTypes[stack[top]] == Type.LONG
                                    && argsTypes[stack[top]] == Type.DOUBLE) {
                                return null;
                            }
                            if (stack[top - 1] == -2 || stack[top - 1] >= 0
                                    && argsTypes[stack[top - 1]] == Type.LONG
                                    && argsTypes[stack[top - 1]] == Type.DOUBLE) {
                                return null;
                            }
                            stack[top - 1] = -1;
                            stack[top] = -1;
                            stack[++top] = -1;
                            break;
                        case Opcodes.DUP_X2:
                            if (stack[top] == -2 || stack[top] >= 0
                                    && argsTypes[stack[top]] == Type.LONG
                                    && argsTypes[stack[top]] == Type.DOUBLE) {
                                return null;
                            }
                            if (stack[top - 1] != -2 || stack[top - 1] >= 0
                                    && argsTypes[stack[top - 1]] != Type.LONG
                                    && argsTypes[stack[top - 1]] != Type.DOUBLE) {
                                return null;
                            }
                            stack[top - 1] = -1;
                            stack[top] = -2;
                            stack[++top] = -1;
                            break;
                        case Opcodes.DUP2:
                            if (stack[top] != -2 || stack[top] >= 0
                                    && argsTypes[stack[top]] != Type.LONG
                                    && argsTypes[stack[top]] != Type.DOUBLE) {
                                return null;
                            }
                            stack[top + 1] = stack[top];
                            top++;
                            break;
                        case Opcodes.DUP2_X1:
                            if (stack[top] != -2 || stack[top] >= 0
                                    && argsTypes[stack[top]] != Type.LONG
                                    && argsTypes[stack[top]] != Type.DOUBLE) {
                                return null;
                            }
                            if (stack[top - 1] == -2 || stack[top - 1] >= 0
                                    && argsTypes[stack[top - 1]] == Type.LONG
                                    && argsTypes[stack[top - 1]] == Type.DOUBLE) {
                                return null;
                            }
                            stack[top - 1] = -2;
                            stack[top] = -1;
                            stack[++top] = -2;
                            break;
                        case Opcodes.DUP2_X2:
                            if (stack[top] != -2 || stack[top] >= 0
                                    && argsTypes[stack[top]] != Type.LONG
                                    && argsTypes[stack[top]] != Type.DOUBLE) {
                                return null;
                            }
                            if (stack[top - 1] != -2 || stack[top - 1] >= 0
                                    && argsTypes[stack[top - 1]] != Type.LONG
                                    && argsTypes[stack[top - 1]] != Type.DOUBLE) {
                                return null;
                            }
                            stack[top - 1] = -2;
                            stack[top] = -2;
                            stack[++top] = -2;
                            break;
                        case Opcodes.SWAP:
                            stack[top - 1] = -1;
                            stack[top] = -1;
                            break;
                        case Opcodes.INEG:
                        case Opcodes.FNEG:
                        case Opcodes.I2F:
                        case Opcodes.L2I:
                        case Opcodes.L2F:
                        case Opcodes.F2I:
                        case Opcodes.D2I:
                        case Opcodes.D2F:
                        case Opcodes.I2B:
                        case Opcodes.I2C:
                        case Opcodes.I2S:
                        case Opcodes.ARRAYLENGTH:
                            stack[top] = -1;
                            break;
                        case Opcodes.LNEG:
                        case Opcodes.DNEG:
                        case Opcodes.I2L:
                        case Opcodes.I2D:
                        case Opcodes.L2D:
                        case Opcodes.F2L:
                        case Opcodes.F2D:
                        case Opcodes.D2L:
                            stack[top] = -2;
                            break;
                        case Opcodes.LRETURN:
                        case Opcodes.DRETURN:
                            abstractInsnNode = null;
                            top--;
                            while (top >= 0) {
                                if (stack[top] == -2) {
                                    insnList.add(new InsnNode(Opcodes.DUP2_X2));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                } else if (stack[top] == -1) {
                                    insnList.add(new InsnNode(Opcodes.DUP2_X1));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                } else if (argsTypes[stack[top]] == Type.LONG || argsTypes[stack[top]] == Type.DOUBLE) {
                                    insnList.add(new InsnNode(Opcodes.DUP2_X2));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                } else {
                                    insnList.add(new InsnNode(Opcodes.DUP2_X1));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                }
                                top--;
                            }
                            while (nextVar >= 0) {
                                if (argsTypes[nextVar] == Type.LONG || argsTypes[nextVar] == Type.DOUBLE) {
                                    insnList.add(new InsnNode(Opcodes.DUP2_X2));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                } else {
                                    insnList.add(new InsnNode(Opcodes.DUP2_X1));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                }
                                nextVar--;
                            }
                            break;
                        case Opcodes.IRETURN:
                        case Opcodes.FRETURN:
                        case Opcodes.ARETURN:
                            abstractInsnNode = null;
                        case Opcodes.ATHROW:
                            top--;
                            while (top >= 0) {
                                if (stack[top] == -2) {
                                    insnList.add(new InsnNode(Opcodes.DUP_X2));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                } else if (stack[top] == -1) {
                                    insnList.add(new InsnNode(Opcodes.DUP_X1));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                } else if (argsTypes[stack[top]] == Type.LONG || argsTypes[stack[top]] == Type.DOUBLE) {
                                    insnList.add(new InsnNode(Opcodes.DUP_X2));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                } else {
                                    insnList.add(new InsnNode(Opcodes.DUP_X1));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                }
                                top--;
                            }
                            while (nextVar >= 0) {
                                if (argsTypes[nextVar] == Type.LONG || argsTypes[nextVar] == Type.DOUBLE) {
                                    insnList.add(new InsnNode(Opcodes.DUP_X2));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                } else {
                                    insnList.add(new InsnNode(Opcodes.DUP_X1));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                    insnList.add(new InsnNode(Opcodes.POP));
                                }
                                nextVar--;
                            }
                            break;
                        case Opcodes.RETURN:
                            abstractInsnNode = null;
                            while (top >= 0) {
                                if (stack[top] == -2) {
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                } else if (stack[top] == -1) {
                                    insnList.add(new InsnNode(Opcodes.POP));
                                } else if (argsTypes[stack[top]] == Type.LONG || argsTypes[stack[top]] == Type.DOUBLE) {
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                } else {
                                    insnList.add(new InsnNode(Opcodes.POP));
                                }
                                top--;
                            }
                            while (nextVar >= 0) {
                                if (argsTypes[nextVar] == Type.LONG || argsTypes[nextVar] == Type.DOUBLE) {
                                    insnList.add(new InsnNode(Opcodes.POP2));
                                } else {
                                    insnList.add(new InsnNode(Opcodes.POP));
                                }
                                nextVar--;
                            }
                            break;
                    }
                    break;
                case AbstractInsnNode.INT_INSN:
                    IntInsnNode intInsnNode = (IntInsnNode) abstractInsnNode;
                    switch (intInsnNode.getOpcode()) {
                        case Opcodes.BIPUSH:
                        case Opcodes.SIPUSH:
                            stack[++top] = -1;
                            break;
                        case Opcodes.NEWARRAY:
                            stack[top] = -1;
                            break;
                    }
                    break;
                case AbstractInsnNode.VAR_INSN:
                    VarInsnNode varInsnNode = (VarInsnNode) abstractInsnNode;
                    switch (varInsnNode.getOpcode()) {
                        case Opcodes.ILOAD:
                        case Opcodes.LLOAD:
                        case Opcodes.FLOAD:
                        case Opcodes.DLOAD:
                        case Opcodes.ALOAD:
                            if (top >= 0) {
                                if (stack[top] < 0) {
                                    return null;
                                }
                                if (stack[top] == varInsnNode.var) {
                                    abstractInsnNode = null;
                                    if (argsTypes[stack[top]] == Type.LONG || argsTypes[stack[top]] == Type.DOUBLE) {
                                        insnList.add(new InsnNode(Opcodes.DUP2));
                                    } else {
                                        insnList.add(new InsnNode(Opcodes.DUP));
                                    }
                                } else if (stack[top] + 1 == varInsnNode.var) {
                                    if (varInsnNode.var > maxVar) {
                                        return null;
                                    } else if (top > 0 && stack[top] == stack[top - 1]) {
                                        return null;
                                    }
                                    abstractInsnNode = null;
                                } else {
                                    return null;
                                }
                            } else {
                                maxVar = nextVar;
                                if (varInsnNode.var > maxVar) {
                                    return null;
                                }
                                nextVar = varInsnNode.var - 1;
                                abstractInsnNode = null;
                            }
                            stack[++top] = varInsnNode.var;
                            break;
                        case Opcodes.ISTORE:
                        case Opcodes.LSTORE:
                        case Opcodes.FSTORE:
                        case Opcodes.DSTORE:
                        case Opcodes.ASTORE:
                        case Opcodes.RET:
                            return null;
                    }
                    break;
                case AbstractInsnNode.TYPE_INSN:
                    TypeInsnNode typeInsnNode = (TypeInsnNode) abstractInsnNode;
                    switch (typeInsnNode.getOpcode()) {
                        case Opcodes.NEW:
                            stack[++top] = -1;
                            break;
                        case Opcodes.ANEWARRAY:
                        case Opcodes.INSTANCEOF:
                            stack[top] = -1;
                            break;
                        case Opcodes.CHECKCAST:
                            break;
                    }
                    break;
                case AbstractInsnNode.FIELD_INSN:
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNode;
                    switch (fieldInsnNode.getOpcode()) {
                        case Opcodes.GETSTATIC:
                            if (Type.getType(fieldInsnNode.desc) == Type.LONG_TYPE || Type.getType(fieldInsnNode.desc) == Type.DOUBLE_TYPE) {
                                stack[++top] = -2;
                            } else {
                                stack[++top] = -1;
                            }
                            break;
                        case Opcodes.PUTSTATIC:
                            top--;
                            break;
                        case Opcodes.GETFIELD:
                            if (Type.getType(fieldInsnNode.desc) == Type.LONG_TYPE || Type.getType(fieldInsnNode.desc) == Type.DOUBLE_TYPE) {
                                stack[top] = -2;
                            } else {
                                stack[top] = -1;
                            }
                            break;
                        case Opcodes.PUTFIELD:
                            top -= 2;
                            break;
                    }
                    break;
                case AbstractInsnNode.METHOD_INSN:
                    MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
                    top -= getArgsTypes(methodInsnNode.desc, methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC).length;
                    if (Type.getType(methodInsnNode.desc).getReturnType() != Type.VOID_TYPE) {
                        if (Type.getType(methodInsnNode.desc).getReturnType() == Type.LONG_TYPE || Type.getType(methodInsnNode.desc).getReturnType() == Type.DOUBLE_TYPE) {
                            stack[++top] = -2;
                        } else {
                            stack[++top] = -1;
                        }
                    }
                    break;
                case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                    InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) abstractInsnNode;
                    top -= getArgsTypes(invokeDynamicInsnNode.desc, true).length;
                    stack[++top] = -1;
                    break;
                case AbstractInsnNode.LDC_INSN:
                    LdcInsnNode ldcInsnNode = (LdcInsnNode) abstractInsnNode;
                    if (ldcInsnNode.cst instanceof Long || ldcInsnNode.cst instanceof Double) {
                        stack[++top] = -2;
                    } else {
                        stack[++top] = -1;
                    }
                    break;
                case AbstractInsnNode.JUMP_INSN:
                case AbstractInsnNode.IINC_INSN:
                case AbstractInsnNode.TABLESWITCH_INSN:
                case AbstractInsnNode.LOOKUPSWITCH_INSN:
                case AbstractInsnNode.FRAME:
                    return null;
                case AbstractInsnNode.MULTIANEWARRAY_INSN:
                    MultiANewArrayInsnNode multiANewArrayInsnNode = (MultiANewArrayInsnNode) abstractInsnNode;
                    top -= multiANewArrayInsnNode.dims;
                    stack[++top] = -1;
                    break;
                case AbstractInsnNode.LABEL:
                    LabelNode labelNode = (LabelNode) abstractInsnNode;
                    labels.put(labelNode, new LabelNode());
                    break;
                case AbstractInsnNode.LINE:
            }
            if (abstractInsnNode != null) {
                insnList.add(abstractInsnNode.clone(labels));
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

    private static String getItemId(String cls, String mtd, String desc) {
        return cls + "->" + mtd + desc;
    }

    private static class Manager {

        private final Map<String, Item> items;

        Manager(ClassNode cn) {
            items = new HashMap<>();
            @SuppressWarnings("unchecked")
            List<MethodNode> methodNodes = cn.methods;
            methodNodes.stream().filter(it -> checkMethod(cn, it)).forEach(it -> {
                InsnList insnList = getInsnList(it);
                if (insnList == null) {
                    return;
                }
                items.put(getItemId(cn.name, it.name, it.desc), new Item(insnList, it.maxStack));
            });
        }

        void prepare() {
            items.values().forEach(Item::prepare);
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
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                Item item = items.get(getItemId(methodInsn.owner, methodInsn.name, methodInsn.desc));
                if (item == null) {
                    continue;
                }
                mn.instructions.insertBefore(methodInsn, item.cloneInsn());
                itr.remove();
                mn.maxStack += item.stackSize;
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
            Iterator<AbstractInsnNode> itr = item.insnList.iterator();
            while (itr.hasNext()) {
                AbstractInsnNode abstractInsnNode = itr.next();
                if (abstractInsnNode.getType() != AbstractInsnNode.METHOD_INSN) {
                    continue;
                }
                MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
                Item another = items.get(getItemId(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc));
                if (another == null || another.contains > 0) {
                    continue;
                }
                item.replaceInvoke(itr, methodInsnNode, another);
                b = true;
            }
            return b;
        }

        private class Item {

            final InsnList insnList;
            int stackSize;
            int contains;

            Item(InsnList insnList, int stackSize) {
                this.insnList = insnList;
                this.stackSize = stackSize;
            }

            void prepare() {
                if (contains != 0) {
                    throw new IllegalStateException();
                }
                @SuppressWarnings("unchecked")
                Iterator<AbstractInsnNode> itr = insnList.iterator();
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
                Iterator<AbstractInsnNode> itr = insnList.iterator();
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