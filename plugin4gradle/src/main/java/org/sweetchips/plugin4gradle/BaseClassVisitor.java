package org.sweetchips.plugin4gradle;

import org.objectweb.asm.ClassVisitor;

import java.nio.file.Path;

public class BaseClassVisitor extends ClassVisitor {

    protected final void setUnused() {
        Util.CLASS_UNUSED.set(true);
    }

    protected final Path getFilePath() {
        return Util.CLASS_FILE_PATH.get();
    }

    protected final void createClass(Path path, byte[] bytes) {
        Util.CLASS_CREATE.get().accept(path, bytes);
    }

    public BaseClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }
}
