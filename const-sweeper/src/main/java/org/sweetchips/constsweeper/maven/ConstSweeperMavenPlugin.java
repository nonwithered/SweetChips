package org.sweetchips.constsweeper.maven;

import org.sweetchips.constsweeper.ConstSweeperContext;
import org.sweetchips.maven.common.AbstractMavenPlugin;

import java.io.File;

final class ConstSweeperMavenPlugin extends AbstractMavenPlugin<ConstSweeperContext> {

    @Override
    protected final String getName() {
        return ConstSweeperContext.NAME;
    }

    public ConstSweeperMavenPlugin(int asmApi, File basedir) {
        super(asmApi, basedir);
    }
}
