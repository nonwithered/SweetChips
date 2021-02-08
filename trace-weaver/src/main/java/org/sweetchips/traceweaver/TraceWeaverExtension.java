package org.sweetchips.traceweaver;

import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.BiFunction;

public class TraceWeaverExtension {

    private int mDepth = Integer.MAX_VALUE;

    private Collection<String> mIgnore = new HashSet<>();

    {
        mIgnore.add(Util.TRACE_WRAPPER_CLASS_NAME);
    }

    int getDepth() {
        return mDepth;
    }

    boolean isIgnored(String clazz, String method) {
        String name = clazz.replaceAll("/", ".");
        if (method == null) {
            return mIgnore.contains(name);
        } else {
            return mIgnore.contains(name + "#" + method);
        }
    }

    public void attach(String name) {
        TraceWeaverContext.getPlugin().attach(name);
    }

    public void maxDepth(int max) {
        if (max < 0) {
            throw new IllegalArgumentException(String.valueOf(max));
        }
        mDepth = max;
    }

    public void ignore(String... name) {
        mIgnore.addAll(Arrays.asList(name));
    }

    public void sectionName(BiFunction<ClassInfo, MethodInfo, String> sectionName) {
        if (sectionName == null) {
            throw new NullPointerException();
        }
        TraceWeaverContext.setSectionName(sectionName);
    }
}
