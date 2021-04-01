package org.sweetchips.traceweaver;

import org.sweetchips.gradle.common.AbstractExtension;
import org.sweetchips.traceweaver.ext.ClassInfo;
import org.sweetchips.traceweaver.ext.MethodInfo;

import java.util.Arrays;
import java.util.function.BiFunction;

public class TraceWeaverExtension extends AbstractExtension<TraceWeaverPlugin> {

    public TraceWeaverExtension(TraceWeaverPlugin plugin) {
        super(plugin);
        ignore(Util.TRACE_WRAPPER_CLASS_NAME.replace("/", "."));
    }

    private int mDepth = Integer.MAX_VALUE;

    private MemberScope mIgnore = newMemberScope();

    private MemberScope mNotice = newMemberScope();

    private BiFunction<ClassInfo, MethodInfo, String> mSectionName = (classInfo, methodInfo) ->
            classInfo.name.replaceAll("/", ".")
                    + "#"
                    + methodInfo.name;

    String getSectionName(ClassInfo classInfo, MethodInfo methodInfo) {
        return mSectionName.apply(classInfo, methodInfo);
    }

    int getDepth() {
        return mDepth;
    }

    boolean isIgnored(String clazz, String member) {
        return mIgnore.contains(clazz, member) && !mNotice.contains(clazz, member);
    }

    public void attach(String name) {
        getPlugin().onAttach(name);
    }

    public void ignore(String... name) {
        Arrays.asList(name).forEach(mIgnore::add);
    }

    public void notice(String... name) {
        Arrays.asList(name).forEach(mNotice::add);
    }

    public void maxDepth(int max) {
        if (max < 0) {
            throw new IllegalArgumentException(String.valueOf(max));
        }
        mDepth = max;
    }

    public void sectionName(BiFunction<ClassInfo, MethodInfo, String> sectionName) {
        if (sectionName == null) {
            throw new NullPointerException();
        }
        mSectionName = sectionName;
    }
}
