package org.sweetchips.visitors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TransformTask extends RecursiveAction {

    private static final int ASM_API = Opcodes.ASM5;

    private final Path mPath;

    private TransformTask(Path path) {
        mPath = path;
    }

    static void transform(Collection<Path> paths) {
        paths.stream()
                .map(TransformTask::fork)
                .collect(Collectors.toList())
                .forEach(ForkJoinTask::join);
    }

    @Override
    protected void compute() {
        try {
            Path in = mPath;
            if (Files.isDirectory(in)) {
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
                    ClassVisitor cv = transformClassVisitor(cw);
                    cr.accept(cv, ClassReader.EXPAND_FRAMES);
                    output.write(cw.toByteArray());
                }
                if (Files.deleteIfExists(in)) {
                    Files.move(out, out.resolveSibling(in.getFileName()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ForkJoinTask<?> fork(Path path) {
        return new TransformTask(path).fork();
    }

    private static String tempSuffix() {
        return "." + System.currentTimeMillis() + ".tmp";
    }

    private static ClassVisitor prepareClassVisitor() {
        return newInstance(null,
                HidePrepareClassVisitor.class,
                UncheckcastPrepareClassVisitor.class);
    }

    private static ClassVisitor transformClassVisitor(ClassWriter cw) {
        return newInstance(cw,
                HideTransformClassVisitor.class,
                UncheckcastTransformClassVisitor.class);
    }

    @SafeVarargs
    private static ClassVisitor newInstance(ClassVisitor visitor, Class<? extends ClassVisitor>... clazzes) {
        AtomicReference<ClassVisitor> ref = new AtomicReference<>(visitor);
        Arrays.asList(clazzes).forEach((clazz) -> ref.set(newInstance(ASM_API, ref.get(), clazz)));
        return ref.get();
    }

    private static ClassVisitor newInstance(int api, ClassVisitor cv, Class<? extends ClassVisitor> clazz) {
        try {
            Constructor<? extends ClassVisitor> constructor = clazz.getConstructor(int.class, ClassVisitor.class);
            constructor.setAccessible(true);
            return constructor.newInstance(api, cv);
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

