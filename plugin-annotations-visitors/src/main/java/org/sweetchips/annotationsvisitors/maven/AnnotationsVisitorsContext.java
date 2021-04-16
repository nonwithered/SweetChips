package org.sweetchips.annotationsvisitors.maven;

import org.sweetchips.annotations.Inline;
import org.sweetchips.annotationsvisitors.HideTransformClassNode;
import org.sweetchips.annotationsvisitors.InlinePrepareClassNode;
import org.sweetchips.annotationsvisitors.InlineTransformClassNode;
import org.sweetchips.annotationsvisitors.UncheckcastTransformClassNode;
import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.ClassVisitorFactory;
import org.sweetchips.platform.jvm.WorkflowSettings;

import java.util.concurrent.ConcurrentHashMap;

public final class AnnotationsVisitorsContext extends BasePluginContext {

    public static final String NAME = "AnnotationsVisitors";

    @Override
    public void onAttach(WorkflowSettings settings) {
        settings.addTransformFirst(ClassVisitorFactory.fromClassNode(HideTransformClassNode.class));
        settings.addTransformFirst(ClassVisitorFactory.fromClassNode(UncheckcastTransformClassNode.class));
        settings.addPrepareBefore(it -> it.put(Inline.class.getName(), new ConcurrentHashMap<>()));
        settings.addPrepareLast(ClassVisitorFactory.fromClassNode(InlinePrepareClassNode.class));
        settings.addTransformLast(ClassVisitorFactory.fromClassNode(InlineTransformClassNode.class));
        settings.addTransformAfter(it -> it.remove(Inline.class.getName()));
    }
}
