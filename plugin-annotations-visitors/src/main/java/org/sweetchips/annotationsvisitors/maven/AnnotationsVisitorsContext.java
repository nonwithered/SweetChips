package org.sweetchips.annotationsvisitors.maven;

import org.sweetchips.annotationsvisitors.HideTransformClassNode;
import org.sweetchips.annotationsvisitors.UncheckcastTransformClassNode;
import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.ClassVisitorFactory;
import org.sweetchips.platform.jvm.WorkflowSettings;

public final class AnnotationsVisitorsContext extends BasePluginContext {

    public static final String NAME = "AnnotationsVisitors";

    @Override
    public void onAttach(WorkflowSettings settings) {
        settings.addTransformFirst(ClassVisitorFactory.fromClassNode(HideTransformClassNode.class));
        settings.addTransformFirst(ClassVisitorFactory.fromClassNode(UncheckcastTransformClassNode.class));
    }
}
