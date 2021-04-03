package org.sweetchips.annotationsvisitors.maven;

import org.sweetchips.maven.java.AbstractMavenPlugin;

import java.io.File;

final class AnnotationsVisitorsMavenPlugin extends AbstractMavenPlugin<AnnotationsVisitorsContext> {

    @Override
    protected String getName() {
        return AnnotationsVisitorsContext.NAME;
    }

    public AnnotationsVisitorsMavenPlugin(int asmApi, File basedir) {
        super(asmApi, basedir);
    }
}
