package org.sweetchips.traceweaver;

import org.gradle.api.Project;
import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

import java.util.function.BiFunction;

final class TraceWeaverContext {

    private static TraceWeaverExtension sExtension;

    private static Project sProject;

    private static TraceWeaverPlugin sPlugin;

    private static BiFunction<ClassInfo, MethodInfo, String> sSectionName =
            (classInfo, methodInfo) ->
                    classInfo.name.replaceAll("/", ".")
                            + "#"
                            + methodInfo.name
                            + methodInfo.desc;

    private static TraceWrapperClassNode sTraceWrapperClassNode;

    static void setExtension(TraceWeaverExtension extension) {
        sExtension = extension;
    }

    static TraceWeaverExtension getExtension() {
        return sExtension;
    }

    static void setProject(Project project) {
        sProject = project;
    }

    static Project getProject() {
        return sProject;
    }

    static void setPlugin(TraceWeaverPlugin plugin) {
        sPlugin = plugin;
    }

    static TraceWeaverPlugin getPlugin() {
        return sPlugin;
    }

    static void setSectionName(BiFunction<ClassInfo, MethodInfo, String> sectionName) {
        sSectionName = sectionName;
    }

    static String getSectionName(ClassInfo classInfo, MethodInfo methodInfo) {
        return sSectionName.apply(classInfo, methodInfo);
    }

    static void setClassNode(TraceWrapperClassNode classNode) {
        sTraceWrapperClassNode = classNode;
    }

    static TraceWrapperClassNode getClassNode() {
        return sTraceWrapperClassNode;
    }
}
