package org.sweetchips.constsweeper.maven;

import org.sweetchips.constsweeper.ConstSweeperContext;
import org.sweetchips.maven.common.AbstractMavenPlugin;

import java.io.File;

final class ConstSweeperMavenPlugin extends AbstractMavenPlugin<ConstSweeperContext> {

    public ConstSweeperMavenPlugin(String name, int asmApi, File basedir) {
        super(name, asmApi, basedir);
    }
}
