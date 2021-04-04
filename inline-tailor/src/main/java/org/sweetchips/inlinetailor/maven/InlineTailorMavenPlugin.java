package org.sweetchips.inlinetailor.maven;

import org.apache.maven.plugin.logging.Log;
import org.sweetchips.inlinetailor.InlineTailorContext;
import org.sweetchips.maven.java.AbstractMavenPlugin;

import java.io.File;

public class InlineTailorMavenPlugin extends AbstractMavenPlugin<InlineTailorContext> {

    @Override
    protected final String getName() {
        return InlineTailorContext.NAME;
    }

    public InlineTailorMavenPlugin(Log log, int asmApi, File basedir) {
        super(log, asmApi, basedir);
    }
}
