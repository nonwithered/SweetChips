package org.sweetchips.traceweaver;

interface Util {

    String NAME = "TraceWeaver";

    String TRACE_CLASS_NAME = "android/os/Trace";

    String TRACE_WRAPPER_CLASS_NAME = "org/sweetchips/traceweaver/TraceWrapper";

    String BEGIN_METHOD_NAME = "begin";

    String BEGIN_METHOD_DESC = "(ILjava/lang/String;)V";

    String END_METHOD_NAME = "end";

    String END_METHOD_DESC = "(I)V";

    String BEGIN_SECTION_METHOD_NAME = "beginSection";

    String BEGIN_SECTION_METHOD_DESC = "(Ljava/lang/String;)V";

    String END_SECTION_METHOD_NAME = "endSection";

    String END_SECTION_METHOD_DESC = "()V";

    String TRACE_WRAPPER_SOURCE = "/resources/" + TRACE_WRAPPER_CLASS_NAME + ".java";
}
