package org.sweetchips.traceweaver;

import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

import java.util.function.BiFunction;

public final class TraceWeaverContext extends BasePluginContext {

    public static final String NAME = "TraceWeaver";

    static final String TRACE_CLASS_NAME = "android/os/Trace";
    static final String TRACE_WRAPPER_CLASS_NAME = TraceWrapper.class.getName().replaceAll("\\.", "/");
    static final String BEGIN_METHOD_NAME = "begin";
    static final String BEGIN_METHOD_DESC = "(IL" + String.class.getName().replaceAll("\\.", "/") + ";)V";
    static final String END_METHOD_NAME = "end";
    static final String END_METHOD_DESC = "(I)V";
    static final String BEGIN_SECTION_METHOD_NAME = "beginSection";
    static final String BEGIN_SECTION_METHOD_DESC = "(L" + String.class.getName().replaceAll("\\.", "/") + ";)V";
    static final String END_SECTION_METHOD_NAME = "endSection";
    static final String END_SECTION_METHOD_DESC = "()V";
    static final String TRACE_WRAPPER_SOURCE = "/resources/" + TRACE_WRAPPER_CLASS_NAME + ".java";

    private int mDepth = Integer.MAX_VALUE;

    private BiFunction<ClassInfo, MethodInfo, String> mSectionName = (classInfo, methodInfo) ->
            classInfo.name.replaceAll("/", ".")
                    + "#"
                    + methodInfo.name;

    public String getSectionName(ClassInfo classInfo, MethodInfo methodInfo) {
        return mSectionName.apply(classInfo, methodInfo);
    }

    public int getDepth() {
        return mDepth;
    }

    public void setMaxDepth(int max) {
        if (max < 0) {
            throw new IllegalArgumentException(String.valueOf(max));
        }
        mDepth = max;
    }

    public void setSectionName(BiFunction<ClassInfo, MethodInfo, String> sectionName) {
        if (sectionName == null) {
            throw new NullPointerException();
        }
        mSectionName = sectionName;
    }
}