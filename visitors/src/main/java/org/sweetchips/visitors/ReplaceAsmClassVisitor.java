package org.sweetchips.visitors;

import org.objectweb.asm.*;

class ReplaceAsmClassVisitor extends ClassVisitor {

    private static final String BEFORE = "org/objectweb/asm/";

    private static final String AFTER = "jdk/internal/org/objectweb/asm/";

    private static String replace(String string) {
        if (string == null) {
            return null;
        }
        string = string.replaceAll(AFTER, BEFORE);
        string = string.replaceAll(BEFORE, AFTER);
        return string;
    }

    public ReplaceAsmClassVisitor(ClassVisitor cv) {
        super(Util.ASM_API.get(), cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        name = replace(name);
        signature = replace(signature);
        superName = replace(superName);
        for (int i = 0; interfaces != null && i < interfaces.length; i++) {
            interfaces[i] = replace(interfaces[i]);
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
        for (int i = 0; exceptions != null && i < exceptions.length; i++) {
            exceptions[i] = replace(exceptions[i]);
        }
        return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
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
                for (int i = 0; local != null && i < local.length; i++) {
                    Object arg = local[i];
                    if (arg instanceof String) {
                        local[i] = replace((String) arg);
                    }
                }
                super.visitFrame(type, nLocal, local, nStack, stack);
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
                desc = replace(desc);
                for (int i = 0; bsmArgs != null && i < bsmArgs.length; i++) {
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
                                handle.isInterface()
                        );
                    }
                }
                super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
            }
        };
    }

}

