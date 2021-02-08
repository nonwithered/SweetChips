package org.sweetchips.plugin4gradle;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.SecondaryInput;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;

import org.objectweb.asm.ClassVisitor;
import org.sweetchips.plugin4gradle.util.ClassesUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Launcher {

    static {
        UnionContext.setExtension(new UnionExtension());
    }

    private static final Scanner sScanner = new Scanner(System.in);

    private static final UnionContext sContext = UnionContext.getInstance(null);

    private static final Collection<Class<? extends ClassVisitor>> sVisitors = new ArrayList<>();

    private static Invocation sInvocation;

    private static String sTag;

    private static UnionTransform sUnionTransform;

    public static void main(String[] args) throws Throwable {
        init(Arrays.asList(args));
        prepare();
        transform();
        start();
    }

    private static void init(Collection<String> paths) {
        Path output = Paths.get(nextLine());
        Collection<Path> input = paths.stream().map(Paths::get).collect(Collectors.toList());
        sInvocation = new Invocation(output, input);
        sTag = nextLine();
    }

    private static void prepare() {
        while (sScanner.hasNextLine()) {
            String name = nextLine();
            if (name.equals(sTag)) {
                break;
            } else {
                addClassVisitor(name);
            }
        }
        sVisitors.forEach(it -> UnionContext.addClassVisitor(AbstractPlugin.ActionType.TRANSFORM, AbstractPlugin.ActionMode.LAST, null, it));
        sVisitors.clear();
    }

    private static void transform() {
        while (sScanner.hasNextLine()) {
            String name = nextLine();
            addClassVisitor(name);
        }
        sVisitors.forEach(it -> UnionContext.addClassVisitor(AbstractPlugin.ActionType.TRANSFORM, AbstractPlugin.ActionMode.LAST, null, it));
        sUnionTransform = new UnionTransform(sContext);
    }

    private static void start() throws TransformException, InterruptedException, IOException {
        sUnionTransform.transform(sInvocation);
    }

    private static String nextLine() {
        String string;
        do {
            string = sScanner.nextLine();
        } while (string.length() == 0);
        return string;
    }

    @SuppressWarnings("unchecked")
    private static void addClassVisitor(String name) {
        Class<?> clazz;
        try {
            clazz = Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (ClassVisitor.class.isAssignableFrom(clazz)) {
            sVisitors.add((Class<? extends ClassVisitor>) clazz);
        } else {
            throw new RuntimeException(name);
        }
    }

    static class Invocation implements TransformInvocation {

        final TransformOutputProvider outputProvider;

        final Collection<TransformInput> inputs;

        Invocation(Path output, Collection<Path> input) {
            outputProvider = new OutputProvider(output);
            inputs = input.stream().map(Input::new).collect(Collectors.toList());
        }

        @Override
        public TransformOutputProvider getOutputProvider() {
            return outputProvider;
        }

        @Override
        public Collection<TransformInput> getInputs() {
            return inputs;
        }

        @Override
        public Context getContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<TransformInput> getReferencedInputs() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<SecondaryInput> getSecondaryInputs() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isIncremental() {
            throw new UnsupportedOperationException();
        }
    }

    static class Input implements TransformInput {

        final Collection<JarInput> jarInputs = new ArrayList<>();

        final Collection<DirectoryInput> directoryInputs = new ArrayList<>();

        Input(Path path) {
            if (Files.isDirectory(path)) {
                directoryInputs.add(new Directory(path));
            } else if (path.getFileName().toString().endsWith(".jar")) {
                jarInputs.add(new Jar(path));
            }
        }

        @Override
        public Collection<JarInput> getJarInputs() {
            return jarInputs;
        }

        @Override
        public Collection<DirectoryInput> getDirectoryInputs() {
            return directoryInputs;
        }
    }

    static class Jar implements JarInput {

        final Path path;

        Jar(Path path) {
            this.path = path;
        }

        @Override
        public Status getStatus() {
            return Status.CHANGED;
        }

        @Override
        public String getName() {
            return path.getFileName().toString();
        }

        @Override
        public File getFile() {
            return path.toFile();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<ContentType> getContentTypes() {
            return (Set<ContentType>) Collections.EMPTY_SET;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<? super Scope> getScopes() {
            return (Set<? super Scope>) Collections.EMPTY_SET;
        }
    }

    static class Directory implements DirectoryInput {

        final Path path;

        final Map<File, Status> changedFiles;

        Directory(Path path) {
            this.path = path;
            changedFiles = new HashMap<>();
            path.forEach(it -> changedFiles.put(it.toFile(), Status.CHANGED));
        }

        @Override
        public Map<File, Status> getChangedFiles() {
            return changedFiles;
        }

        @Override
        public String getName() {
            return path.getFileName().toString();
        }

        @Override
        public File getFile() {
            return path.toFile();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<ContentType> getContentTypes() {
            return (Set<ContentType>) Collections.EMPTY_SET;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<? super Scope> getScopes() {
            return (Set<? super Scope>) Collections.EMPTY_SET;
        }
    }

    static class OutputProvider implements TransformOutputProvider {

        final Path path;

        OutputProvider(Path path) {
            this.path = path;
            if (!Files.isDirectory(path) && !path.toFile().mkdirs()) {
                throw new RuntimeException(path.toString() + !Files.isDirectory(path));
            }
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public File getContentLocation(String name, Set<QualifiedContent.ContentType> types, Set<? super QualifiedContent.Scope> scopes, Format format) {
            switch (format) {
                case DIRECTORY:
                    return path.toFile();
                case JAR:
                    return Paths.get(path.toString(), Paths.get(name).getFileName().toString()).toFile();
                default:
                    throw new RuntimeException();
            }
        }
    }
}
