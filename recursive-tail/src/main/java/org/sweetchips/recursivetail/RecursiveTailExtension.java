package org.sweetchips.recursivetail;

import org.sweetchips.gradle.common.AbstractExtension;

import java.util.Arrays;

public class RecursiveTailExtension extends AbstractExtension<RecursiveTailPlugin> {

    private final MemberScope mIgnore = newMemberScope();
    private final MemberScope mNotice = newMemberScope();

    public RecursiveTailExtension(RecursiveTailPlugin plugin) {
        super(plugin);
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
}