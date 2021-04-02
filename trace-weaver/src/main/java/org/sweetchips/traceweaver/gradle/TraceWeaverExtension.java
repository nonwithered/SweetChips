package org.sweetchips.traceweaver.gradle;

import org.sweetchips.gradle.common.AbstractExtension;
import org.sweetchips.traceweaver.TraceWeaverContext;
import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

import java.util.function.BiFunction;

public class TraceWeaverExtension extends AbstractExtension<TraceWeaverContext> {

    public void maxDepth(int max) {
        getContext().setMaxDepth(max);
    }

    public void setSectionName(BiFunction<ClassInfo, MethodInfo, String> sectionName) {
        getContext().setSectionName(sectionName);
    }
}
