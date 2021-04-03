package org.sweetchips.constsweeper.gradle;

import org.sweetchips.constsweeper.ConstSweeperContext;
import org.sweetchips.gradle.common.AbstractGradlePlugin;

public final class ConstSweeperGradlePlugin extends AbstractGradlePlugin<ConstSweeperExtension> {

    @Override
    protected final String getName() {
        return ConstSweeperContext.NAME;
    }
}