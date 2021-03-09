package org.sweetchips.common.jvm;

import org.objectweb.asm.ClassVisitor;

import java.util.Map;

public interface ClassVisitorFactory {

    ClassVisitor newInstance(int api, ClassVisitor cv, Map<?, ?> ext);
}
