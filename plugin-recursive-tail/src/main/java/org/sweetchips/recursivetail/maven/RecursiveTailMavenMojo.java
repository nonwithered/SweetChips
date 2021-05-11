package org.sweetchips.recursivetail.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.Opcodes;
import org.sweetchips.maven.java.AbstractMavenPlugin;
import org.sweetchips.recursivetail.RecursiveTailContext;

import java.io.File;

@Mojo(name = "recursivetail")
public class RecursiveTailMavenMojo extends AbstractMojo implements AbstractMavenPlugin<RecursiveTailContext> {

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
        return RecursiveTailContext.NAME;
    }
}
