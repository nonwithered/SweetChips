package org.sweetchips.inlinetailor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

final class InlineTailorStackMirror {

    private int mTop;
    private int mBegin;
    private int mEnd;
    private final int[] mStack;
    private final int[] mArgsTypes;
    private final InsnList mInsnList = new InsnList();

    InlineTailorStackMirror(int size, int[] argsTypes) {
        int length = argsTypes.length;
        mStack = new int[size + length];
        mArgsTypes = argsTypes;
        mTop = length - 1;
        mBegin = length;
        mEnd = length;
        for (int i = 0; i < length; i++) {
            mStack[i] = i;
        }
    }

    InsnList getInsnList() {
        return mInsnList;
    }

    boolean addInsnNode(InsnNode insnNode) {
        switch (insnNode.getOpcode()) {
            case Opcodes.NOP:
            case Opcodes.ATHROW:
                break;
            case Opcodes.ACONST_NULL: case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0: case Opcodes.ICONST_1: case Opcodes.ICONST_2:
            case Opcodes.ICONST_3: case Opcodes.ICONST_4: case Opcodes.ICONST_5:
            case Opcodes.FCONST_0: case Opcodes.FCONST_1: case Opcodes.FCONST_2:
                push1();
                break;
            case Opcodes.LCONST_0: case Opcodes.LCONST_1:
            case Opcodes.DCONST_0: case Opcodes.DCONST_1:
                push2();
                break;
            case Opcodes.IALOAD: case Opcodes.FALOAD: case Opcodes.AALOAD: case Opcodes.BALOAD: case Opcodes.CALOAD: case Opcodes.SALOAD:
            case Opcodes.IADD: case Opcodes.FADD: case Opcodes.ISUB: case Opcodes.FSUB:
            case Opcodes.IMUL: case Opcodes.FMUL: case Opcodes.IDIV: case Opcodes.FDIV:
            case Opcodes.IREM: case Opcodes.FREM:
            case Opcodes.ISHL: case Opcodes.ISHR: case Opcodes.IUSHR:
            case Opcodes.IAND: case Opcodes.IOR: case Opcodes.IXOR:
            case Opcodes.LCMP: case Opcodes.FCMPL: case Opcodes.FCMPG: case Opcodes.DCMPL: case Opcodes.DCMPG:
                pop(2);
                push1();
                break;
            case Opcodes.LALOAD: case Opcodes.DALOAD:
            case Opcodes.LADD: case Opcodes.DADD: case Opcodes.LSUB: case Opcodes.DSUB:
            case Opcodes.LMUL: case Opcodes.DMUL: case Opcodes.LDIV: case Opcodes.DDIV:
            case Opcodes.LREM: case Opcodes.DREM:
            case Opcodes.LSHL: case Opcodes.LSHR: case Opcodes.LUSHR:
            case Opcodes.LAND: case Opcodes.LOR: case Opcodes.LXOR:
                pop(2);
                push2();
                break;
            case Opcodes.IASTORE: case Opcodes.LASTORE: case Opcodes.FASTORE: case Opcodes.DASTORE:
            case Opcodes.AASTORE: case Opcodes.BASTORE: case Opcodes.CASTORE: case Opcodes.SASTORE:
                pop(3);
                break;
            case Opcodes.MONITORENTER: case Opcodes.MONITOREXIT:
            case Opcodes.POP:
                if (isBiggerTop()) {
                    return false;
                }
                pop(1);
                break;
            case Opcodes.POP2:
                if (!isBiggerTop()) {
                    return false;
                }
                pop(1);
                break;
            case Opcodes.DUP:
                if (isBiggerTop()) {
                    return false;
                }
                pop(1);
                push1();
                push1();
                break;
            case Opcodes.DUP_X1:
                if (isBiggerTop()) {
                    return false;
                }
                pop(1);
                if (isBiggerTop()) {
                    return false;
                }
                pop(1);
                push1();
                push1();
                push1();
                break;
            case Opcodes.DUP_X2:
                if (isBiggerTop()) {
                    return false;
                }
                pop(1);
                if (!isBiggerTop()) {
                    return false;
                }
                pop(1);
                push1();
                push2();
                push1();
                break;
            case Opcodes.DUP2:
                if (!isBiggerTop()) {
                    return false;
                }
                pop(1);
                push2();
                push2();
                break;
            case Opcodes.DUP2_X1:
                if (!isBiggerTop()) {
                    return false;
                }
                pop(1);
                if (isBiggerTop()) {
                    return false;
                }
                pop(1);
                push2();
                push1();
                push2();
                break;
            case Opcodes.DUP2_X2:
                if (!isBiggerTop()) {
                    return false;
                }
                pop(1);
                if (!isBiggerTop()) {
                    return false;
                }
                pop(1);
                push2();
                push2();
                push2();
                break;
            case Opcodes.SWAP:
                if (isBiggerTop()) {
                    return false;
                }
                pop(1);
                if (isBiggerTop()) {
                    return false;
                }
                pop(1);
                push1();
                push1();
                break;
            case Opcodes.INEG: case Opcodes.FNEG:
            case Opcodes.I2F: case Opcodes.L2I: case Opcodes.L2F:
            case Opcodes.F2I: case Opcodes.D2I: case Opcodes.D2F:
            case Opcodes.I2B: case Opcodes.I2C: case Opcodes.I2S:
            case Opcodes.ARRAYLENGTH:
                pop(1);
                push1();
                break;
            case Opcodes.LNEG: case Opcodes.DNEG:
            case Opcodes.I2L: case Opcodes.I2D: case Opcodes.L2D:
            case Opcodes.F2L: case Opcodes.F2D: case Opcodes.D2L:
                pop(1);
                push2();
                break;
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                pop(1);
                while (mTop >= 0) {
                    if (isBiggerTop()) {
                        mInsnList.add(new InsnNode(Opcodes.DUP2_X2));
                        mInsnList.add(new InsnNode(Opcodes.POP2));
                        mInsnList.add(new InsnNode(Opcodes.POP2));
                    } else {
                        mInsnList.add(new InsnNode(Opcodes.DUP2_X1));
                        mInsnList.add(new InsnNode(Opcodes.POP2));
                        mInsnList.add(new InsnNode(Opcodes.POP));
                    }
                    mTop--;
                }
                return true;
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
                pop(1);
                while (mTop >= 0) {
                    if (isBiggerTop()) {
                        mInsnList.add(new InsnNode(Opcodes.DUP_X2));
                        mInsnList.add(new InsnNode(Opcodes.POP));
                        mInsnList.add(new InsnNode(Opcodes.POP2));
                    } else {
                        mInsnList.add(new InsnNode(Opcodes.DUP_X1));
                        mInsnList.add(new InsnNode(Opcodes.POP));
                        mInsnList.add(new InsnNode(Opcodes.POP));
                    }
                    mTop--;
                }
                return true;
            case Opcodes.RETURN:
                while (mTop >= 0) {
                    if (isBiggerTop()) {
                        mInsnList.add(new InsnNode(Opcodes.POP2));
                    } else {
                        mInsnList.add(new InsnNode(Opcodes.POP));
                    }
                    mTop--;
                }
                return true;
            default:
                throw new IllegalStateException();
        }
        return addAbstractInsnNode(insnNode);
    }

    boolean addIntInsnNode(IntInsnNode intInsnNode) {
        switch (intInsnNode.getOpcode()) {
            case Opcodes.NEWARRAY:
                pop(1);
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                push1();
                break;
            default:
                throw new IllegalStateException();
        }
        return addAbstractInsnNode(intInsnNode);
    }

    boolean addVarInsnNode(VarInsnNode varInsnNode) {
        switch (varInsnNode.getOpcode()) {
            case Opcodes.RET:
            case Opcodes.ISTORE: case Opcodes.LSTORE:
            case Opcodes.FSTORE: case Opcodes.DSTORE:
            case Opcodes.ASTORE:
                return false;
            case Opcodes.ILOAD: case Opcodes.LLOAD:
            case Opcodes.FLOAD: case Opcodes.DLOAD:
            case Opcodes.ALOAD:
                if (mTop < 0) {
                    return false;
                }
                if (top() < 0) {
                    return false;
                }
                if (mTop < varInsnNode.var) {
                    return false;
                }
                if (mTop < mBegin) {
                    mBegin = varInsnNode.var;
                    mEnd = mBegin;
                } else {
                    if (mEnd == varInsnNode.var) {
                        sweepTop();
                        if (isBigger(mArgsTypes[mStack[mEnd]])) {
                            mInsnList.add(new InsnNode(Opcodes.DUP2));
                        } else {
                            mInsnList.add(new InsnNode(Opcodes.DUP));
                        }
                        mStack[++mTop] = mStack[mEnd];
                    } else if (mEnd + 1 == varInsnNode.var) {
                        if (top() == mStack[mEnd]) {
                            return false;
                        }
                        mEnd++;
                    } else {
                        return false;
                    }
                }
                break;
            default:
                throw new IllegalStateException();
        }
        return true;
    }

    boolean addTypeInsnNode(TypeInsnNode typeInsnNode) {
        switch (typeInsnNode.getOpcode()) {
            case Opcodes.ANEWARRAY:
            case Opcodes.CHECKCAST:
            case Opcodes.INSTANCEOF:
                pop(1);
            case Opcodes.NEW:
                push1();
                break;
            default:
                throw new IllegalStateException();
        }
        return addAbstractInsnNode(typeInsnNode);
    }

    boolean addFieldInsnNode(FieldInsnNode fieldInsnNode) {
        switch (fieldInsnNode.getOpcode()) {
            case Opcodes.GETSTATIC:
                push(fieldInsnNode.desc);
                break;
            case Opcodes.PUTSTATIC:
                pop(1);
                break;
            case Opcodes.GETFIELD:
                pop(1);
                push(fieldInsnNode.desc);
                break;
            case Opcodes.PUTFIELD:
                pop(2);
                break;
            default:
                throw new IllegalStateException();
        }
        return addAbstractInsnNode(fieldInsnNode);
    }

    boolean addMethodInsnNode(MethodInsnNode methodInsnNode) {
        invoke(methodInsnNode.desc, methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC);
        return addAbstractInsnNode(methodInsnNode);
    }

    boolean addInvokeDynamicInsnNode(InvokeDynamicInsnNode invokeDynamicInsnNode) {
        invoke(invokeDynamicInsnNode.desc, true);
        return addAbstractInsnNode(invokeDynamicInsnNode);
    }

    boolean addLdcInsnNode(LdcInsnNode ldcInsnNode) {
        if (ldcInsnNode.cst instanceof Long || ldcInsnNode.cst instanceof Double) {
            push2();
        } else {
            push1();
        }
        return addAbstractInsnNode(ldcInsnNode);
    }

    boolean addMultiANewArrayInsnNode(MultiANewArrayInsnNode multiANewArrayInsnNode) {
        pop(multiANewArrayInsnNode.dims);
        push1();
        return addAbstractInsnNode(multiANewArrayInsnNode);
    }

    boolean addAbstractInsnNode(AbstractInsnNode abstractInsnNode) {
        mInsnList.add(abstractInsnNode);
        return true;
    }

    private void sweepTop() {
        if (mTop < 0 || top() < 0) {
            return;
        }
        while (mTop >= mBegin && top() > mStack[mEnd]) {
            if (isBigger(mArgsTypes[top()])) {
                mInsnList.add(new InsnNode(Opcodes.POP2));
            } else {
                mInsnList.add(new InsnNode(Opcodes.POP));
            }
            mTop--;
        }
    }

    private void pop(int n) {
        sweepTop();
        while (n-- > 0) {
            if (mTop >= mBegin && top() == mStack[mEnd]) {
                if (mTop == 0 || mStack[mTop - 1] != mStack[mEnd]) {
                    mEnd--;
                }
            }
            mTop--;
        }
    }

    private void push1() {
        sweepTop();
        mStack[++mTop] = -1;
        if (mTop == mBegin) {
            mBegin++;
        }
    }

    private void push2() {
        sweepTop();
        mStack[++mTop] = -2;
        if (mTop == mBegin) {
            mBegin++;
        }
    }

    private void push(String desc) {
        push(Type.getType(desc));
    }

    private void push(Type type) {
        if (isBigger(type)) {
            push2();
        } else {
            push1();
        }
    }

    private int top() {
        return mStack[mTop];
    }

    private void invoke(String desc, boolean isStatic) {
        pop(InlineTailorContext.getArgsTypes(desc, isStatic).length);
        Type type = Type.getType(desc).getReturnType();
        if (type != Type.VOID_TYPE) {
            push(type);
        }
    }

    private boolean isBiggerTop() {
        sweepTop();
        return top() == -2 || top() >= 0 && isBigger(mArgsTypes[top()]);
    }

    private static boolean isBigger(Type type) {
        return isBigger(type.getSort());
    }

    private static boolean isBigger(int type) {
        return type == Type.LONG || type == Type.DOUBLE;
    }
}
