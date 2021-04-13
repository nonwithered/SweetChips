package org.sweetchips.annotationsvisitors.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.Opcodes;

import java.io.File;

@Mojo(name = "annotationsvisitors", defaultPhase = LifecyclePhase.COMPILE)
public class AnnotationsVisitorsMavenMojo extends AbstractMojo {

    @Parameter(defaultValue = "" + Opcodes.ASM5)
    private int asmApi;

    @Parameter(defaultValue = "${basedir}", readonly = true)
    private File basedir;

    @Override
    public void execute() {
        AnnotationsVisitorsMavenPlugin plugin = new AnnotationsVisitorsMavenPlugin(getLog(), asmApi, basedir);
        plugin.execute();
    }
}
