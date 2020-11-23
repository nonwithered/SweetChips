package org.sweetchips.plugin4gradle;

import org.objectweb.asm.ClassVisitor;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface UnionContext {

    UnionExtension EXT = new UnionExtension();

    Collection<Class<? extends ClassVisitor>> PREPARE = new ConcurrentLinkedQueue<>();

    Collection<Class<? extends ClassVisitor>> DUMP = new ConcurrentLinkedQueue<>();
}
