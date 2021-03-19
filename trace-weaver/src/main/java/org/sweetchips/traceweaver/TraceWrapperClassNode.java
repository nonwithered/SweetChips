package org.sweetchips.traceweaver;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.utility.ClassesUtil;
import org.sweetchips.utility.FilesUtil;

final class TraceWrapperClassNode extends ClassNode {

    TraceWrapperClassNode(int api) {
        super(api);
        new ClassReader(ClassesUtil.compile(Util.TRACE_WRAPPER_CLASS_NAME,
                () -> new String(FilesUtil.readFrom(getClass().getResourceAsStream(Util.TRACE_WRAPPER_SOURCE))),
                System.err::println)).accept(this, ClassReader.EXPAND_FRAMES);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access | Opcodes.ACC_SYNTHETIC, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access | Opcodes.ACC_SYNTHETIC, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(Util.BEGIN_SECTION_METHOD_NAME) && desc.equals(Util.BEGIN_SECTION_METHOD_DESC)
                || name.equals(Util.END_SECTION_METHOD_NAME) && desc.equals(Util.END_SECTION_METHOD_DESC)) {
            return null;
        }
        return new MethodVisitor(api, super.visitMethod(access | Opcodes.ACC_SYNTHETIC, name, desc, signature, exceptions)) {

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                if (owner.equals(Util.TRACE_WRAPPER_CLASS_NAME)) {
                    if (name.equals(Util.BEGIN_SECTION_METHOD_NAME) && desc.equals(Util.BEGIN_SECTION_METHOD_DESC)
                            || name.equals(Util.END_SECTION_METHOD_NAME) && desc.equals(Util.END_SECTION_METHOD_DESC)) {
                        owner = Util.TRACE_CLASS_NAME;
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        };
    }
}
