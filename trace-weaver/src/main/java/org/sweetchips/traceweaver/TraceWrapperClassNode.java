package org.sweetchips.traceweaver;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

final class TraceWrapperClassNode extends ClassNode {

    private final AtomicBoolean mInit = new AtomicBoolean();

    TraceWrapperClassNode(int api) {
        super(api);
    }

    final void init() {
        if (mInit.get() || !mInit.compareAndSet(false, true)) {
            return;
        }
        try (InputStream input = getBytecode()) {
            ClassReader cr = new ClassReader(input);
            ClassVisitor cv = new TraceWrapperClassVisitor(api, this);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getBytecode() {
        try (InputStream input = getClass().getResourceAsStream(Util.TRACE_WRAPPER_SOURCE);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            TraceWrapperJavaFileObject javaFileObject =
                    new TraceWrapperJavaFileObject(input, output);
            TraceWrapperJavaFileManager javaFileManager =
                    new TraceWrapperJavaFileManager(compiler.getStandardFileManager(null, null, null),
                            javaFileObject);
            Boolean b = compiler.getTask(null,
                    javaFileManager,
                    null,
                    null,
                    null,
                    Collections.singletonList(javaFileObject)).call();
            if (!b) {
                throw new IOException();
            }
            byte[] bytes = output.toByteArray();
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class TraceWrapperJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private final JavaFileObject mFileObject;

        private TraceWrapperJavaFileManager(JavaFileManager fileManager, JavaFileObject fileObject) {
            super(fileManager);
            mFileObject = fileObject;
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
            return mFileObject;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            return mFileObject;
        }
    }

    private static final class TraceWrapperJavaFileObject extends SimpleJavaFileObject {

        private final InputStream mInput;
        private final OutputStream mOutput;

        private TraceWrapperJavaFileObject(InputStream input, OutputStream output) {
            super(TraceWeaverContext.getProject()
                    .getRootDir().toPath()
                    .resolve("build")
                    .resolve(Paths.get("/")
                            .relativize(Paths.get(Util.TRACE_WRAPPER_SOURCE))).toUri(),
                    Kind.SOURCE);
            mInput = input;
            mOutput = output;
        }

        @Override
        public InputStream openInputStream() {
            return mInput;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            try (InputStream input = openInputStream();
                 Scanner scanner = new Scanner(input)
            ) {
                while (scanner.hasNextLine()) {
                    stringBuilder.append(scanner.nextLine());
                }
            }
            return stringBuilder;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return mOutput;
        }
    }

    private static final class TraceWrapperClassVisitor extends ClassVisitor {

        private TraceWrapperClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            access |= Opcodes.ACC_SYNTHETIC;
            super.visit(version, access | Opcodes.ACC_PUBLIC, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            access |= Opcodes.ACC_SYNTHETIC;
            if (name.equals(Util.BEGIN_SECTION_METHOD_NAME) && desc.equals(Util.BEGIN_SECTION_METHOD_DESC)
                    || name.equals(Util.END_SECTION_METHOD_NAME) && desc.equals(Util.END_SECTION_METHOD_DESC)) {
                return null;
            }
            return new TraceWrapperMethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions));
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            access |= Opcodes.ACC_SYNTHETIC;
            return super.visitField(access, name, desc, signature, value);
        }
    }

    private static final class TraceWrapperMethodVisitor extends MethodVisitor {

        private TraceWrapperMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (owner.equals(Util.TRACE_WRAPPER_CLASS_NAME)) {
                if (name.equals(Util.BEGIN_SECTION_METHOD_NAME) && desc.equals(Util.BEGIN_SECTION_METHOD_DESC)
                        || name.equals(Util.END_SECTION_METHOD_NAME) && desc.equals(Util.END_SECTION_METHOD_DESC)) {
                    owner = Util.TRACE_CLASS_NAME;
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
