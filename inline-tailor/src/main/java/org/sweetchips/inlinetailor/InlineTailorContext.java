package org.sweetchips.inlinetailor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
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
import org.sweetchips.platform.jvm.BasePluginContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class InlineTailorContext extends BasePluginContext {

    public static final String NAME = "InlineTailor";

    static String getItemId(String cls, String mtd, String desc) {
        return cls + "->" + mtd + desc;
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

    static boolean checkAccess(int access, int flag) {
        return (access & flag) != 0;
    }

    static InsnList getInsnList(MethodNode mn) {
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
}
