package org.sweetchips.recursivetail.maven;

import org.sweetchips.maven.common.AbstractMavenPlugin;
import org.sweetchips.recursivetail.RecursiveTailContext;

import java.io.File;

public class RecursiveTailMavenPlugin extends AbstractMavenPlugin<RecursiveTailContext> {

    @Override
    protected final String getName() {
        return RecursiveTailContext.NAME;
    }

    public RecursiveTailMavenPlugin(int asmApi, File basedir) {
        super(asmApi, basedir);
    }
}
