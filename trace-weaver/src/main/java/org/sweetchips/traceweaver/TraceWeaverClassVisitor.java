package org.sweetchips.traceweaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.sweetchips.plugin4gradle.BaseClassVisitor;
import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

public final class TraceWeaverClassVisitor extends BaseClassVisitor {

    private boolean mIsIgnored;

    private ClassInfo mClassInfo;

    public TraceWeaverClassVisitor(int api) {
        this(api, null);
    }

    public TraceWeaverClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    private void init(String name) {
        mIsIgnored = TraceWeaverContext.getExtension().isIgnored(name, null);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        init(name);
        mClassInfo = new ClassInfo(version, access, name, signature, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (mIsIgnored || TraceWeaverContext.getExtension().isIgnored(mClassInfo.name, name)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        MethodInfo methodInfo = new MethodInfo(access, name, desc, signature, exceptions);
        String sectionName = TraceWeaverContext.sectionName(mClassInfo, methodInfo);
        return new TraceWeaverMethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions), sectionName);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (mClassInfo.name.equals(Util.TRACE_WRAPPER_CLASS_NAME)) {
            setUnused();
        }
    }
}
