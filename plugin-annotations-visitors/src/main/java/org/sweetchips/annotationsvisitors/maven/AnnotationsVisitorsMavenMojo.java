package org.sweetchips.annotationsvisitors.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.Opcodes;
import org.sweetchips.maven.java.AbstractMavenPlugin;

import java.io.File;

@Mojo(name = "annotationsvisitors")
public final class AnnotationsVisitorsMavenMojo extends AbstractMojo implements AbstractMavenPlugin<AnnotationsVisitorsContext> {

    @Parameter(defaultValue = "" + Opcodes.ASM5)
    private int asmApi;

    @Parameter(defaultValue = "${basedir}", readonly = true)
    private File basedir;

    @Override
    public String getName() {
        return AnnotationsVisitorsContext.NAME;
    }
}
