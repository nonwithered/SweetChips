package org.sweetchips.traceweaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.sweetchips.platform.jvm.BaseClassVisitor;
import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

public final class TraceWeaverTransformClassVisitor extends BaseClassVisitor<TraceWeaverContext> {

    private ClassInfo mClassInfo;

    public TraceWeaverTransformClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    private void init(String name) {
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        init(name);
        mClassInfo = new ClassInfo(version, access, name, signature, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (getContext().isIgnored(mClassInfo.name, name)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        MethodInfo methodInfo = new MethodInfo(access, name, desc, signature, exceptions);
        String sectionName = getContext().getSectionName(mClassInfo, methodInfo);
        return new TraceWeaverMethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions), sectionName).withContext(getContext());
    }
}
