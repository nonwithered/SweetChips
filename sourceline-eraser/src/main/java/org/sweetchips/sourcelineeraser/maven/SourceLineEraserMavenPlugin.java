package org.sweetchips.sourcelineeraser.maven;

import org.sweetchips.maven.common.AbstractMavenPlugin;
import org.sweetchips.sourcelineeraser.SourceLineEraserContext;

import java.io.File;

public class SourceLineEraserMavenPlugin extends AbstractMavenPlugin<SourceLineEraserContext> {

    @Override
    protected final String getName() {
        return SourceLineEraserContext.NAME;
    }

    public SourceLineEraserMavenPlugin(int asmApi, File basedir) {
        super(asmApi, basedir);
    }
}
