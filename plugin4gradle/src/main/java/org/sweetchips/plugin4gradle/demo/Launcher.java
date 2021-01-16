package org.sweetchips.plugin4gradle.demo;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.SecondaryInput;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;

import org.objectweb.asm.ClassVisitor;
import org.sweetchips.plugin4gradle.UnionContext;
import org.sweetchips.plugin4gradle.UnionExtension;
import org.sweetchips.plugin4gradle.UnionTransform;
import org.sweetchips.plugin4gradle.Util;

import java.io.File;
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

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        UnionContext context = new UnionContext(Util.NAME, new UnionExtension());
        try (Scanner scanner = new Scanner(System.in)) {
            String tag;
            do {
                tag = scanner.nextLine();
            } while (tag.length() == 0);
            Path output = Paths.get(tag);
            Collection<Path> input = Arrays.stream(args).map(Paths::get).collect(Collectors.toList());
            do {
                tag = scanner.nextLine();
            } while (tag.length() == 0);
            while (scanner.hasNextLine()) {
                String string;
                do {
                    string = scanner.nextLine();
                } while (string.length() == 0);
                if (tag != null && tag.equals(string)) {
                    tag = null;
                    continue;
                }
                Class<? extends ClassVisitor> clazz = (Class<? extends ClassVisitor>) Class.forName(string);
                if (tag != null) {
                    context.addPrepare(clazz);
                } else {
                    context.addDump(clazz);
                }
            }
            new UnionTransform(context).transform(new Invocation(output, input));
        } catch (Exception e) {
            e.printStackTrace();
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
            if (path.toFile().isDirectory()) {
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
            if (!path.toFile().exists() || !path.toFile().isDirectory()) {
                throw  new IllegalArgumentException();
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
                    throw new AssertionError();
            }
        }
    }
}
