package org.sweetchips.plugin4gradle;

import org.objectweb.asm.ClassVisitor;

import java.nio.file.Path;

public class BaseClassVisitor extends ClassVisitor {

    protected final void setUnused() {
        Util.CLASS_UNUSED.set(true);
    }

    protected final void createClasses(Path path, byte[] bytes) {
        Util.CLASS_CREATE.get().accept(path, bytes);
    }

    protected BaseClassVisitor(int api) {
        this(api, null);
    }

    protected BaseClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }
}
