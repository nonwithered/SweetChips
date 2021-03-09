package org.sweetchips.traceweaver;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

final class TraceWrapperClassNode extends ClassNode {

    TraceWrapperClassNode(int api) {
        super(api);
    }

    @Override
    public void accept(ClassVisitor cv) {
        init();
        super.accept(cv);
    }

    private void init() {
        try {
            ClassReader cr = new ClassReader(getTraceWrapper());
            cr.accept(this, ClassReader.EXPAND_FRAMES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access | Opcodes.ACC_SYNTHETIC, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access | Opcodes.ACC_SYNTHETIC, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(Util.BEGIN_SECTION_METHOD_NAME) && desc.equals(Util.BEGIN_SECTION_METHOD_DESC)
                || name.equals(Util.END_SECTION_METHOD_NAME) && desc.equals(Util.END_SECTION_METHOD_DESC)) {
            return null;
        }
        return new MethodVisitor(api, super.visitMethod(access | Opcodes.ACC_SYNTHETIC, name, desc, signature, exceptions)) {

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
        };
    }

    private byte[] getTraceWrapper() throws IOException {
        try (InputStream input = getClass().getResourceAsStream(Util.TRACE_WRAPPER_SOURCE);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            JavaFileObjectImpl javaFileObject =
                    new JavaFileObjectImpl(input, output);
            JavaFileManagerImpl javaFileManager =
                    new JavaFileManagerImpl(compiler.getStandardFileManager(null, null, null),
                            javaFileObject);
            boolean b = compiler.getTask(null,
                    javaFileManager,
                    null,
                    null,
                    null,
                    Collections.singletonList(javaFileObject)).call();
            if (!b) {
                throw new IOException();
            }
            return output.toByteArray();
        }
    }

    private static final class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

        private final JavaFileObject mFileObject;

        private JavaFileManagerImpl(JavaFileManager fileManager, JavaFileObject fileObject) {
            super(fileManager);
            mFileObject = fileObject;
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) {
            return mFileObject;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            return mFileObject;
        }
    }

    private static final class JavaFileObjectImpl extends SimpleJavaFileObject {

        private final InputStream mInput;
        private final OutputStream mOutput;

        private JavaFileObjectImpl(InputStream input, OutputStream output) {
            super(TraceWeaverPlugin.getInstance().getProject()
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
            return getContent(openInputStream());
        }

        @Override
        public OutputStream openOutputStream() {
            return mOutput;
        }

        private static String getContent(InputStream inputStream) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            try (InputStream input = inputStream;
                 Scanner scanner = new Scanner(inputStream)
            ) {
                while (scanner.hasNextLine()) {
                    stringBuilder.append(scanner.nextLine());
                }
            }
            return stringBuilder.toString();
        }
    }
}
