package org.sweetchips.inlinetailor.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.Opcodes;
import org.sweetchips.inlinetailor.InlineTailorPlusContext;
import org.sweetchips.maven.java.AbstractMavenPlugin;

import java.io.File;

@Mojo(name = "inlinetailorplus")
public final class InlineTailorPlusMavenMojo extends AbstractMojo implements AbstractMavenPlugin<InlineTailorPlusContext> {

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
        return InlineTailorPlusContext.NAME;
    }
}
