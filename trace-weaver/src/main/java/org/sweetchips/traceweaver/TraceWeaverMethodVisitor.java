package org.sweetchips.traceweaver;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class TraceWeaverMethodVisitor extends MethodVisitor {

    private TraceWeaverContext mContext;

    TraceWeaverMethodVisitor withContext(TraceWeaverContext context) {
        mContext = context;
        return this;
    }

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

    private String sectionName(String sectionName) {
        if (sectionName != null) {
            int maxLength = 127;
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
        visitMethodInsn(Opcodes.INVOKESTATIC, TraceWeaverContext.TRACE_WRAPPER_CLASS_NAME, TraceWeaverContext.BEGIN_METHOD_NAME, TraceWeaverContext.BEGIN_METHOD_DESC, false);
    }

    private void endSection() {
        visitDepth();
        visitMethodInsn(Opcodes.INVOKESTATIC, TraceWeaverContext.TRACE_WRAPPER_CLASS_NAME, TraceWeaverContext.END_METHOD_NAME, TraceWeaverContext.END_METHOD_DESC, false);
    }

    private void visitDepth() {
        int maxDepth = mContext.getDepth();
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
