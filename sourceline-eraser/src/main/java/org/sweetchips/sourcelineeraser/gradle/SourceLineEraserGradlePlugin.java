package org.sweetchips.sourcelineeraser.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import org.sweetchips.sourcelineeraser.SourceLineEraserContext;

public class SourceLineEraserGradlePlugin extends AbstractGradlePlugin<SourceLineEraserExtension> {

    @Override
    public final String getName() {
        return SourceLineEraserContext.NAME;
    }
}
