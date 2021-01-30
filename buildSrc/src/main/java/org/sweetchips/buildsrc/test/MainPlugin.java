package org.sweetchips.buildsrc.test;

import org.gradle.api.Project;
import org.objectweb.asm.ClassVisitor;
import org.sweetchips.plugin4gradle.BasePlugin;
import org.sweetchips.visitors.HidePrepareClassVisitor;
import org.sweetchips.visitors.HideTransformClassVisitor;
import org.sweetchips.visitors.UncheckcastPrepareClassVisitor;
import org.sweetchips.visitors.UncheckcastTransformClassVisitor;

import java.util.Arrays;
import java.util.Collection;

class MainPlugin extends BasePlugin {

    @Override
    protected void onApply(Project project) {
    }

    @Override
    protected Collection<Class<? extends org.objectweb.asm.ClassVisitor>> onPrepare() {
        return Arrays.asList(HidePrepareClassVisitor.class, UncheckcastPrepareClassVisitor.class);
    }

    @Override
    protected Collection<Class<? extends ClassVisitor>> onTransform() {
        return Arrays.asList(HideTransformClassVisitor.class, UncheckcastTransformClassVisitor.class);
    }
}