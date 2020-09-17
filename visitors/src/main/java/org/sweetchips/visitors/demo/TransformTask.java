package org.sweetchips.visitors.demo;

import org.sweetchips.visitors.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

public class TransformTask extends RecursiveAction {

    private final Path mPath;

    TransformTask(Path path) {
        mPath = path;
    }

    static void transform(Collection<Path> paths) {
        try {
            paths.stream()
                    .map(TransformTask::fork)
                    .collect(Collectors.toList())
                    .forEach(ForkJoinTask::join);
        } catch (Throwable e) {
            while (e instanceof AssertionError) {
                e = e.getCause();
            }
            throw new AssertionError(e);
        }
    }

    @Override
    protected void compute() {
        try {
            Path in = mPath;
            if (in.toFile().isDirectory()) {
                Files.list(in)
                        .map(TransformTask::fork)
                        .collect(Collectors.toList())
                        .forEach(ForkJoinTask::join);
                return;
            }
            if (!in.getFileName().toString().endsWith(".class")) {
                return;
            }
            Path out = Paths.get(in.getParent().toString(),
                    in.getFileName() + tempSuffix());
            try (OutputStream output = Files.newOutputStream(out)) {
                try (InputStream input = Files.newInputStream(in)) {
                    ClassReader cr = new ClassReader(input);
                    ClassVisitor cv = prepareClassVisitor();
                    cr.accept(cv, ClassReader.EXPAND_FRAMES);
                }
                try (InputStream input = Files.newInputStream(in)) {
                    ClassWriter cw = new ClassWriter(0);
                    ClassReader cr = new ClassReader(input);
                    ClassVisitor cv = dumpClassVisitor(cw);
                    cr.accept(cv, ClassReader.EXPAND_FRAMES);
                    output.write(cw.toByteArray());
                }
                if (in.toFile().delete()) {
                    if (!out.toFile().renameTo(in.toFile())) {
                        throw new IOException();
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static ForkJoinTask<?> fork(Path path) {
        return new TransformTask(path).fork();
    }

    private static String tempSuffix() {
        return "." + System.currentTimeMillis() + ".tmp";
    }

    private static ClassVisitor prepareClassVisitor() {
        return Util.newInstance(Opcodes.ASM5,
                HidePrepareClassVisitor::new,
                UncheckcastPrepareClassVisitor::new);
    }

    private static ClassVisitor dumpClassVisitor(ClassWriter cw) {
        return Util.newInstance(cw,
                HideDumpClassVisitor::new,
                UncheckcastDumpClassVisitor::new);
    }

}

