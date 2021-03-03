package org.sweetchips.traceweaver;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class TraceWeaverMethodVisitor extends MethodVisitor {

    private final String mSectionName;

    TraceWeaverMethodVisitor(int api, MethodVisitor mv, String sectionName) {
        super(api, mv);
        mSectionName = sectionName(sectionName);
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if (mSectionName != null) {
            beginSection();
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (mSectionName != null) {
            switch (opcode) {
                case Opcodes.IRETURN:
                case Opcodes.LRETURN:
                case Opcodes.FRETURN:
                case Opcodes.DRETURN:
                case Opcodes.ARETURN:
                case Opcodes.RETURN:
                case Opcodes.ATHROW:
                    endSection();
                default:
            }
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        if (mSectionName != null) {
            maxStack += 2;
        }
        super.visitMaxs(maxStack, maxLocals);
    }

    private static String sectionName(String sectionName) {
        if (sectionName != null) {
            int maxLength = TraceWeaverPlugin.getInstance().getExtension().getLength();
            int length = sectionName.length();
            if (length > maxLength) {
                sectionName = sectionName.substring(length - maxLength);
            }
        }
        return sectionName;
    }

    private void beginSection() {
        visitDepth();
        visitLdcInsn(mSectionName);
        visitMethodInsn(Opcodes.INVOKESTATIC, Util.TRACE_WRAPPER_CLASS_NAME, Util.BEGIN_METHOD_NAME, Util.BEGIN_METHOD_DESC, false);
    }

    private void endSection() {
        visitDepth();
        visitMethodInsn(Opcodes.INVOKESTATIC, Util.TRACE_WRAPPER_CLASS_NAME, Util.END_METHOD_NAME, Util.END_METHOD_DESC, false);
    }

    private void visitDepth() {
        int maxDepth = TraceWeaverPlugin.getInstance().getExtension().getDepth();
        switch (maxDepth) {
            case 0:
                visitInsn(Opcodes.ICONST_0);
                break;
            case 1:
                visitInsn(Opcodes.ICONST_1);
                break;
            case 2:
                visitInsn(Opcodes.ICONST_2);
                break;
            case 3:
                visitInsn(Opcodes.ICONST_3);
                break;
            case 4:
                visitInsn(Opcodes.ICONST_4);
                break;
            case 5:
                visitInsn(Opcodes.ICONST_5);
                break;
            default:
                visitIntInsn(Opcodes.BIPUSH, maxDepth);
        }
    }
}
