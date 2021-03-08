package org.sweetchips.platform.jvm;

import org.objectweb.asm.ClassVisitor;

import java.util.Map;

public interface ClassVisitorFactory {

    ClassVisitor newInstance(int api, ClassVisitor cv, Map<String, ?> ext);
}
