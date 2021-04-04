package org.sweetchips.sourcelineeraser.maven;

import org.apache.maven.plugin.logging.Log;
import org.sweetchips.maven.java.AbstractMavenPlugin;
import org.sweetchips.sourcelineeraser.SourceLineEraserContext;

import java.io.File;

public class SourceLineEraserMavenPlugin extends AbstractMavenPlugin<SourceLineEraserContext> {

    @Override
    protected final String getName() {
        return SourceLineEraserContext.NAME;
    }

    public SourceLineEraserMavenPlugin(Log log, int asmApi, File basedir) {
        super(log, asmApi, basedir);
    }
}
