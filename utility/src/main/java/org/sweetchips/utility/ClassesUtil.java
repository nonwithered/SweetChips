package org.sweetchips.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.function.Supplier;

import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

public interface ClassesUtil {

    static String toStringMethod(String owner, String name, String desc) {
        return owner + "->" + name + desc;
    }

    static String toStringField(String owner, String name, String desc) {
        return owner + "->" + name + ":" + desc;
    }

    static Type[] getSuperTypeArgs(Class<?> clazz, Class<?> superClazz) {
        while (true) {
            Class<?> temp = clazz.getSuperclass();
            if (temp != superClazz) {
                clazz = temp;
                continue;
            }
            Type type = clazz.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                return ((ParameterizedType) type).getActualTypeArguments();
            }
            throw new IllegalArgumentException(superClazz.getName());
        }
    }

    @SuppressWarnings("unchecked")
    static <T> Class<T> forName(String name) {
        return AsyncUtil.call(() -> (Class<T>) Class.forName(name));
    }

    static <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Class<?>... args) {
        return AsyncUtil.call(() -> clazz.getDeclaredConstructor(args));
    }

    static <T> T newInstance(Constructor<T> constructor, Object... args) {
        return AsyncUtil.call(() ->  constructor.newInstance(args));
    }

    static byte[] compile(String name, Supplier<String> content, DiagnosticListener<JavaFileObject> listener) {
        String str = name.replaceAll("\\.","/");
        return AsyncUtil.call(() -> {
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///" + str + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                        return content.get();
                    }
                };
                JavaFileObject target = new SimpleJavaFileObject(URI.create("string:///" + str + JavaFileObject.Kind.CLASS.extension), JavaFileObject.Kind.CLASS) {
                    @Override
                    public OutputStream openOutputStream() {
                        return bytes;
                    }
                };
                JavaFileManager manager = new ForwardingJavaFileManager<JavaFileManager>(compiler.getStandardFileManager(null, null, null)) {
                    @Override
                    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) {
                        return source;
                    }
                    @Override
                    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
                        return target;
                    }
                };
                DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
                if (!compiler.getTask(null, manager, collector, null, null, Collections.singletonList(source)).call()) {
                    if (listener != null) {
                        collector.getDiagnostics().forEach(listener::report);
                    }
                    throw new IOException();
                }
                return bytes.toByteArray();
            }
        });
    }
}
