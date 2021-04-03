package org.sweetchips.sourcelineeraser.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.util.Arrays;

@Mojo(name = "sourcelineeraser", defaultPhase = LifecyclePhase.COMPILE)
public class SourceLineEraserMavenMojo extends AbstractMojo {

    @Parameter(defaultValue = "" + Opcodes.ASM5)
    private int asmApi;

    @Parameter(defaultValue = "${basedir}", readonly = true)
    private File basedir;

    @Parameter()
    private String[] ignores;

    @Parameter
    private String[] notices;

    @Override
    public void execute() {
        SourceLineEraserMavenPlugin plugin = new SourceLineEraserMavenPlugin(asmApi, basedir);
        if (ignores != null) {
            Arrays.stream(ignores).forEach(plugin.getContext()::addIgnore);
        }
        if (notices != null) {
            Arrays.stream(notices).forEach(plugin.getContext()::addNotice);
        }
        plugin.execute();
    }
}
