package org.sweetchips.sourcelineeraser.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.Opcodes;
import org.sweetchips.maven.java.AbstractMavenPlugin;
import org.sweetchips.sourcelineeraser.SourceLineEraserContext;

import java.io.File;

@Mojo(name = "sourcelineeraser")
public class SourceLineEraserMavenMojo extends AbstractMojo implements AbstractMavenPlugin<SourceLineEraserContext> {

    @Parameter(defaultValue = "" + Opcodes.ASM5)
    private int asmApi;

    @Parameter(defaultValue = "${basedir}", readonly = true)
    private File basedir;

    @Parameter
    private String[] ignores;

    @Parameter
    private String[] notices;

    @Override
    public String getName() {
        return SourceLineEraserContext.NAME;
    }
}
