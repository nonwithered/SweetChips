package org.sweetchips.annotationsvisitors.maven;

import org.apache.maven.plugin.logging.Log;
import org.sweetchips.maven.java.AbstractMavenPlugin;

import java.io.File;

final class AnnotationsVisitorsMavenPlugin extends AbstractMavenPlugin<AnnotationsVisitorsContext> {

    @Override
    protected String getName() {
        return AnnotationsVisitorsContext.NAME;
    }

    public AnnotationsVisitorsMavenPlugin(Log log, int asmApi, File basedir) {
        super(log, asmApi, basedir);
    }
}
