package org.sweetchips.maven.common;

import org.apache.maven.plugin.AbstractMojo;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.platform.jvm.WorkflowSettings;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;

public abstract class AbstractMavenPlugin extends AbstractMojo {

    protected abstract void onExecute(WorkflowSettings settings);

    protected abstract int getAsmApi();

    protected abstract String getName();

    protected abstract Path getBasedir();

    @Override
    public final void execute() {
        JvmContext context = new JvmContext();
        onExecute(new WorkflowProfile(context));
        work(context);
        sweep();
    }

    private void work(JvmContext context) {
        Path from = getClassDir();
        Path to = getTempDir();
        FilesUtil.deleteIfExists(to);
        context.setApi(getAsmApi());
        new SweetchipsJavaMavenTransform(context).transform(from, to);
    }

    private void sweep() {
        Path from = getTempDir();
        Path to = getClassDir();
        FilesUtil.deleteIfExists(to);
        JvmContext context = new JvmContext();
        context.setApi(getAsmApi());
        new SweetchipsJavaMavenTransform(context).transform(from, to);
    }

    private Path getClassDir() {
        return getBasedir()
                .resolve("target")
                .resolve("classes");
    }

    private Path getTempDir() {
        return getBasedir()
                .resolve("target")
                .resolve("intermediates")
                .resolve("transforms")
                .resolve(getName());
    }
}