package org.sweetchips.recursivetail.maven;

import org.apache.maven.plugin.logging.Log;
import org.sweetchips.maven.java.AbstractMavenPlugin;
import org.sweetchips.recursivetail.RecursiveTailContext;

import java.io.File;

public class RecursiveTailMavenPlugin extends AbstractMavenPlugin<RecursiveTailContext> {

    @Override
    protected final String getName() {
        return RecursiveTailContext.NAME;
    }

    public RecursiveTailMavenPlugin(Log log, int asmApi, File basedir) {
        super(log, asmApi, basedir);
    }
}
