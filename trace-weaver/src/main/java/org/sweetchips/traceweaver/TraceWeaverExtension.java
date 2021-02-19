package org.sweetchips.traceweaver;

import org.sweetchips.plugin4gradle.AbstractExtension;
import org.sweetchips.plugin4gradle.AbstractPlugin;
import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.BiFunction;

public class TraceWeaverExtension extends AbstractExtension {

    private int mDepth = Integer.MAX_VALUE;

    private Collection<String> mIgnore = new HashSet<>();

    private Collection<String> mIgnoreExcept = new HashSet<>();

    private TraceWrapperClassNode mClassNode;

    private BiFunction<ClassInfo, MethodInfo, String> mSectionName =
            (classInfo, methodInfo) ->
                    classInfo.name.replaceAll("/", ".")
                            + "#"
                            + methodInfo.name
                            + methodInfo.desc;

    String getSectionName(ClassInfo classInfo, MethodInfo methodInfo) {
        return mSectionName.apply(classInfo, methodInfo);
    }

    void setClassNode(TraceWrapperClassNode classNode) {
        mClassNode = classNode;
    }

    TraceWrapperClassNode getClassNode() {
        return mClassNode;
    }

    int getDepth() {
        return mDepth;
    }

    boolean isIgnored(String clazz, String field) {
        return containsScope(mIgnore, clazz, field) && !containsScope(mIgnoreExcept, clazz, field);
    }

    public void maxDepth(int max) {
        if (max < 0) {
            throw new IllegalArgumentException(String.valueOf(max));
        }
        mDepth = max;
    }

    public void ignore(String... name) {
        addScope(mIgnore, name);
    }

    public void ignoreExcept(String... name) {
        addScope(mIgnoreExcept, name);
    }

    public void sectionName(BiFunction<ClassInfo, MethodInfo, String> sectionName) {
        if (sectionName == null) {
            throw new NullPointerException();
        }
        mSectionName = sectionName;
    }

    private boolean containsScope(Collection<String> scope, String clazz, String field) {
        if (scope.size() <= 0) {
            return false;
        }
        String[] split = clazz.split("/");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            builder.append(split[i]);
            builder.append(".");
            if (scope.contains(builder + "*")) {
                return true;
            }
        }
        builder.append(split[split.length - 1]);
        return scope.contains(builder.toString()) || field != null && scope.contains(builder + "#" + field);
    }

    private void addScope(Collection<String> scope, String[] names) {
        scope.addAll(Arrays.asList(names));
    }

    public TraceWeaverExtension(AbstractPlugin<? extends AbstractExtension> plugin) {
        super(plugin);
        mIgnore.add(Util.TRACE_WRAPPER_CLASS_NAME);
    }
}
