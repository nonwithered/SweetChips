package org.sweetchips.maven.java;

import org.apache.maven.plugin.Mojo;
import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.ClassesUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;

public interface AbstractMavenPlugin<C extends BasePluginContext> extends Mojo {

    @Override
    default void execute() {
        @SuppressWarnings("unchecked")
        Class<C> clazz = (Class<C>) ClassesUtil.getSuperTypeArgs(getClass(), AbstractMavenPlugin.class)[0];
        C context = ClassesUtil.newInstance(ClassesUtil.getDeclaredConstructor(clazz));
        onExecute(context);
        new WorkflowWorker<>(this, context).run();
    }

    default int getAsmApi() {
        return AsyncUtil.call(() -> {
            Field asmApi = getClass().getDeclaredField("asmApi");
            asmApi.setAccessible(true);
            return (int) asmApi.get(this);
        });
    }

    default File getBaseDir() {
        return AsyncUtil.call(() -> {
            Field basedir = getClass().getDeclaredField("basedir");
            basedir.setAccessible(true);
            return (File) basedir.get(this);
        });
    }

    default String[] getIgnores() {
        return AsyncUtil.call(() -> {
            try {
                Field ignores = getClass().getDeclaredField("ignores");
                ignores.setAccessible(true);
                return (String[]) ignores.get(this);
            } catch (NoSuchFieldException e) {
                return null;
            }
        });
    }

    default String[] getNotices() {
        return AsyncUtil.call(() -> {
            try {
                Field notices = getClass().getDeclaredField("notices");
                notices.setAccessible(true);
                return (String[]) notices.get(this);
            } catch (NoSuchFieldException e) {
                return null;
            }
        });
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