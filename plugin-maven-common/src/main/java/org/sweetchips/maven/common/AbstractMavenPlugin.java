package org.sweetchips.maven.common;

import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.platform.jvm.WorkflowSettings;
import org.sweetchips.utility.ClassesUtil;
import org.sweetchips.utility.FilesUtil;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;

public abstract class AbstractMavenPlugin<C extends BasePluginContext> {

    private final C mContext;
    private final String mName;
    private final int mAsmApi;
    private final File mBasedir;

    public final C getContext() {
        return mContext;
    }

    public AbstractMavenPlugin(String name, int asmApi, File basedir) {
        mContext = newContext();
        mName = name;
        mAsmApi = asmApi;
        mBasedir = basedir;
    }

    public final void execute() {
        JvmContext context = new JvmContext();
        mContext.onAttach(new WorkflowProfile(context));
        work(context);
        sweep();
    }

    private C newContext() {
        Type type = getClass();
        while (!(type instanceof ParameterizedType)) {
            type = ((Class<?>) type).getGenericSuperclass();
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        @SuppressWarnings("unchecked")
        Class<C> clazz = (Class<C>) parameterizedType.getActualTypeArguments()[0];
        return ClassesUtil.newInstance(ClassesUtil.getDeclaredConstructor(clazz));
    }

    private void work(JvmContext context) {
        Path from = getClassDir();
        Path to = getTempDir();
        FilesUtil.deleteIfExists(to);
        context.setApi(mAsmApi);
        new SweetchipsJavaMavenTransform(context).transform(from, to);
    }

    private void sweep() {
        Path from = getTempDir();
        Path to = getClassDir();
        FilesUtil.deleteIfExists(to);
        JvmContext context = new JvmContext();
        context.setApi(mAsmApi);
        new SweetchipsJavaMavenTransform(context).transform(from, to);
    }

    private Path getClassDir() {
        return mBasedir.toPath()
                .resolve("target")
                .resolve("classes");
    }

    private Path getTempDir() {
        return mBasedir.toPath()
                .resolve("target")
                .resolve("intermediates")
                .resolve("transforms")
                .resolve(mName);
    }
}