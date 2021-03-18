package org.sweetchips.traceweaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.sweetchips.common.jvm.ClassesSetting;
import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

public final class TraceWeaverTransformClassVisitor extends ClassVisitor {

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
        if (TraceWeaverPlugin.INSTANCE.getExtension().isIgnored(mClassInfo.name, name)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        MethodInfo methodInfo = new MethodInfo(access, name, desc, signature, exceptions);
        String sectionName = TraceWeaverPlugin.INSTANCE.getExtension().getSectionName(mClassInfo, methodInfo);
        return new TraceWeaverMethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions), sectionName);
    }
}
