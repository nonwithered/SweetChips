package org.sweetchips.annotationsvisitors;

import org.objectweb.asm.*;

public class ReplaceNameClassVisitor extends ClassVisitor {

    private final boolean mContains;

    private final String mBefore;

    private final String mAfter;

    public ReplaceNameClassVisitor(int api, ClassVisitor cv, String before, String after) {
        super(api, cv);
        if (before == null || after == null) {
            throw new NullPointerException();
        }
        mContains = after.contains(before);
        mBefore = before;
        mAfter = after;
    }

    private String replace(String string) {
        if (string == null) {
            return null;
        }
        if (mContains) {
            string = string.replaceAll(mAfter, mBefore);
        }
        string = string.replaceAll(mBefore, mAfter);
        return string;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        name = replace(name);
        signature = replace(signature);
        superName = replace(superName);
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i] = replace(interfaces[i]);
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        desc = replace(desc);
        signature = replace(signature);
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        desc = replace(desc);
        signature = replace(signature);
        if (exceptions != null) {
            for (int i = 0; i < exceptions.length; i++) {
                exceptions[i] = replace(exceptions[i]);
            }
        }
        return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
            @Override
            public void visitLdcInsn(Object cst) {
                if (cst instanceof Type) {
                    cst = Type.getType(replace(cst.toString()));
                }
                super.visitLdcInsn(cst);
            }

            @Override
            public void visitTypeInsn(int opcode, String type) {
                type = replace(type);
                super.visitTypeInsn(opcode, type);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                owner = replace(owner);
                desc = replace(desc);
                super.visitFieldInsn(opcode, owner, name, desc);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                owner = replace(owner);
                desc = replace(desc);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }

            @Override
            public void visitMultiANewArrayInsn(String desc, int dims) {
                desc = replace(desc);
                super.visitMultiANewArrayInsn(desc, dims);
            }

            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                type = replace(type);
                super.visitTryCatchBlock(start, end, handler, type);
            }

            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                desc = replace(desc);
                signature = replace(signature);
                super.visitLocalVariable(name, desc, signature, start, end, index);
            }

            @Override
            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                if (local != null) {
                    for (int i = 0; i < local.length; i++) {
                        Object arg = local[i];
                        if (arg instanceof String) {
                            local[i] = replace((String) arg);
                        }
                    }
                }
                super.visitFrame(type, nLocal, local, nStack, stack);
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
                desc = replace(desc);
                if (bsmArgs != null) {
                    for (int i = 0; i < bsmArgs.length; i++) {
                        Object bsmArg = bsmArgs[i];
                        if (bsmArg instanceof String) {
                            String string = (String) bsmArg;
                            bsmArgs[i] = replace(string);
                        } else if (bsmArg instanceof Type) {
                            Type type = (Type) bsmArg;
                            bsmArgs[i] = Type.getType(replace(type.toString()));
                        } else if (bsmArg instanceof Handle) {
                            Handle handle = (Handle) bsmArg;
                            bsmArgs[i] = new Handle(
                                    handle.getTag(),
                                    replace(handle.getOwner()),
                                    handle.getName(),
                                    replace(handle.getDesc()),
                                    handle.isInterface());
                        }
                    }
                }
                super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
            }
        };
    }
}

