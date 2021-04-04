package org.sweetchips.constsweeper.maven;

import org.apache.maven.plugin.logging.Log;
import org.sweetchips.constsweeper.ConstSweeperContext;
import org.sweetchips.maven.java.AbstractMavenPlugin;

import java.io.File;

final class ConstSweeperMavenPlugin extends AbstractMavenPlugin<ConstSweeperContext> {

    @Override
    protected final String getName() {
        return ConstSweeperContext.NAME;
    }

    public ConstSweeperMavenPlugin(Log log, int asmApi, File basedir) {
        super(log, asmApi, basedir);
    }
}
