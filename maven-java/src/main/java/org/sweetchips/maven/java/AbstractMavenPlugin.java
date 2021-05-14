package org.sweetchips.maven.java;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.utility.ClassesUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public interface AbstractMavenPlugin<C extends BasePluginContext> extends Mojo {

    @Override
    default void execute() throws MojoExecutionException, MojoFailureException {
        @SuppressWarnings("unchecked")
        Class<C> clazz = (Class<C>) ClassesUtil.getSuperTypeArgs(getClass(), AbstractMavenPlugin.class)[0];
        C context = ClassesUtil.newInstance(ClassesUtil.getDeclaredConstructor(clazz));
        onExecute(context);
        try {
            new WorkflowWorker<>(this, context).call();
        } catch (ExecutionException e) {
            throw new MojoExecutionException(getName(), e);
        } catch (InterruptedException e) {
            throw new MojoFailureException(getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    default <T> T getParameter(String parameter) {
        try {
            Field field = getClass().getDeclaredField(parameter);
            field.setAccessible(true);
            return (T) field.get(this);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    default int getAsmApi() {
        return getParameter("asmApi");
    }

    default File getBaseDir() {
        return getParameter("basedir");
    }

    default String[] getIgnores() {
        return getParameter("ignores");
    }

    default String[] getNotices() {
        return getParameter("notices");
    }

    default void onExecute(C context) {
        String[] ignores = getIgnores();
        if (ignores != null) {
            Arrays.stream(ignores).forEach(context::addIgnore);
        }
        String[] notices = getNotices();
        if (notices != null) {
            Arrays.stream(notices).forEach(context::addNotice);
        }
    }

    String getName();
}