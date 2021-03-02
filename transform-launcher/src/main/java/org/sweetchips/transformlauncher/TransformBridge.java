package org.sweetchips.transformlauncher;

import org.sweetchips.plugin4gradle.UnionContext;
import org.sweetchips.plugin4gradle.UnionTransform;
import org.sweetchips.plugin4gradle.Util;
import org.sweetchips.transformlauncher.bridge.BaseExtension;
import org.sweetchips.transformlauncher.bridge.ExtensionContainer;
import org.sweetchips.transformlauncher.bridge.Plugin;
import org.sweetchips.transformlauncher.bridge.PluginContainer;
import org.sweetchips.transformlauncher.bridge.Project;
import org.sweetchips.transformlauncher.bridge.Transform;
import org.sweetchips.transformlauncher.bridge.TransformException;
import org.sweetchips.transformlauncher.bridge.TransformInvocation;
import org.sweetchips.plugin4gradle.util.AsyncUtil;
import org.sweetchips.plugin4gradle.util.ClassesUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class TransformBridge {

    @SuppressWarnings("unchecked")
    public static void init(Map<String, String> properties) {
        if (sDebug) {
            throw new IllegalStateException();
        }
        sDebug = true;
        if (properties == null) {
            properties = Collections.EMPTY_MAP;
        }
        sProperties = properties;
        sPlugins.put(Util.APPLICATION, new PluginImpl());
        sPlugins.put(Util.LIBRARY, new PluginImpl());
        sExtensions.put(Util.ANDROID, new BaseExtensionImpl());
    }

    public static void launch(Path in, Path out, Path tmp, BiFunction<Path, Path, TransformInvocation> factory) {
        if (in == null || out == null || tmp == null || factory == null) {
            throw new NullPointerException();
        }
        if (!sDebug || sLaunch) {
            throw new IllegalStateException();
        }
        sLaunch = true;
        sTransforms.add((Transform) (Object) new UnionTransform(new UnionContext(null)));
        AsyncUtil.run(() -> transform(in, out, tmp, factory));
    }

    public static void apply(String clazz) {
        if (!sDebug || sLaunch) {
            throw new IllegalStateException();
        }
        if (sPlugins.containsKey(clazz) || !sProperties.containsKey(clazz)) {
            throw new IllegalArgumentException();
        }
        Plugin<Project> plugin = ClassesUtil.newInstance(ClassesUtil.getDeclaredConstructor(ClassesUtil.forName(sProperties.get(clazz))));
        plugin.apply(sProject);
        sPlugins.put(clazz, plugin);
    }

    @SuppressWarnings("unchecked")
    public static <T> void config(String name, Consumer<T> consumer) {
        consumer.accept((T) sExtensions.get(name));
    }

    private static boolean sDebug;

    private static boolean sLaunch;

    private static Project sProject = new ProjectImpl();

    private static BaseExtension sAndroid = new BaseExtensionImpl();

    private static Map<String, String> sProperties = new HashMap<>();

    private static Map<String, Plugin<Project>> sPlugins = new HashMap<>();

    private static Map<String, Object> sExtensions = new HashMap<>();

    private static final List<Transform> sTransforms = new ArrayList<>();

    private static void transform(Path in, Path out, Path tmp, BiFunction<Path, Path, TransformInvocation> factory) throws TransformException, InterruptedException, IOException {
        for (int i = 0; i < sTransforms.size(); i++) {
            Path src = i == 0 ? in : getTmpDir(tmp, sTransforms.get(i - 1));
            Path desc = i == sTransforms.size() - 1 ? out : getTmpDir(tmp, sTransforms.get(i));
            sTransforms.get(i).transform(factory.apply(src, desc));
        }
    }

    private static final class ProjectImpl implements Project {

        private final PluginContainer mPluginContainer = new PluginContainerImpl();


        private final ExtensionContainer mExtensionContainer = new ExtensionContainerImpl();

        @Override
        public PluginContainer getPlugins() {
            return mPluginContainer;
        }

        @Override
        public ExtensionContainer getExtensions() {
            return mExtensionContainer;
        }
    }

    private static final class ExtensionContainerImpl implements ExtensionContainer {

        @Override
        public <T> T create(String name, Class<T> clazz, Object... args) {
            if (sExtensions.containsKey(name)) {
                throw new IllegalArgumentException();
            }
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                Class<?>[] types = constructor.getParameterTypes();
                if (types.length == args.length) {
                    boolean b = true;
                    for (int i = 0; i < args.length; i++) {
                        if (!types[i].isAssignableFrom(args[i].getClass())) {
                            b = false;
                            break;
                        }
                    }
                    if (b) {
                        @SuppressWarnings("unchecked")
                        T extension = (T) ClassesUtil.newInstance(constructor, args);
                        sExtensions.put(name, extension);
                        return extension;
                    }
                }
            }
            throw new IllegalArgumentException();
        }

        @Override
        public Object getByName(String name) {
            return sExtensions.get(name);
        }
    }

    private static final class BaseExtensionImpl extends BaseExtension {

        @Override
        public void registerTransform(Transform transform, Object... dependencies) {
            sTransforms.add(transform);
        }
    }

    private static final class PluginContainerImpl implements PluginContainer {

        @Override
        public Plugin<Project> findPlugin(String name) {
            return sPlugins.get(name);
        }
    }

    private static final class PluginImpl implements Plugin<Project> {

        @Override
        public void apply(Project arg) {
            throw new UnsupportedOperationException();
        }
    }

    private static Path getTmpDir(Path tmp, Transform transform) {
        String name = transform.getName();
        if (name == null) {
            throw new NullPointerException();
        }
        Path transformsPath = tmp.resolve(name);
        if (!Files.exists(transformsPath)) {
            AsyncUtil.run(() -> Files.createDirectories(transformsPath));
        }
        return transformsPath;
    }

    private TransformBridge() {
        throw new UnsupportedOperationException();
    }
}
