package org.sweetchips.inlinetailor.maven;

import org.apache.maven.plugin.logging.Log;
import org.sweetchips.inlinetailor.InlineTailorPlusContext;
import org.sweetchips.maven.java.AbstractMavenPlugin;

import java.io.File;

public class InlineTailorPlusMavenPlugin extends AbstractMavenPlugin<InlineTailorPlusContext> {

    @Override
    protected final String getName() {
        return InlineTailorPlusContext.NAME;
    }

    public InlineTailorPlusMavenPlugin(Log log, int asmApi, File basedir) {
        super(log, asmApi, basedir);
    }
}

