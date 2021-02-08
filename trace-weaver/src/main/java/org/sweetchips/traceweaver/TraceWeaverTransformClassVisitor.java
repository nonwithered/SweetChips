package org.sweetchips.traceweaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.sweetchips.plugin4gradle.BaseClassVisitor;
import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

public final class TraceWeaverTransformClassVisitor extends BaseClassVisitor {

    private boolean mIsIgnored;

    private ClassInfo mClassInfo;

    public TraceWeaverTransformClassVisitor(int api) {
        this(api, null);
    }

    public TraceWeaverTransformClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    private void init(String name) {
        mIsIgnored = TraceWeaverPlugin.getInstance().getExtension().isIgnored(name, null);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        TraceWeaverPlugin.getInstance().getExtension().getClassNode().init();
        init(name);
        mClassInfo = new ClassInfo(version, access, name, signature, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (mIsIgnored || TraceWeaverPlugin.getInstance().getExtension().isIgnored(mClassInfo.name, name)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        MethodInfo methodInfo = new MethodInfo(access, name, desc, signature, exceptions);
        String sectionName = TraceWeaverPlugin.getInstance().getExtension().getSectionName(mClassInfo, methodInfo);
        return new TraceWeaverMethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions), sectionName);
    }

    @Override
    public void visitEnd() {
        if (mClassInfo.name.equals(Util.TRACE_WRAPPER_CLASS_NAME)) {
            setUnused();
        }
        super.visitEnd();
    }
}
