package org.sweetchips.constsweeper;

import org.sweetchips.gradle.common.AbstractExtension;

import java.util.Arrays;

public class ConstSweeperExtension extends AbstractExtension {

    private MemberScope mIgnore = newMemberScope();

    private MemberScope mNotice = newMemberScope();

    synchronized boolean isIgnored(String clazz, String member) {
        return mIgnore.contains(clazz, member) && !mNotice.contains(clazz, member);
    }

    public void attach(String name) {
        ConstSweeperPlugin.INSTANCE.onAttach(name);
    }

    public void ignore(String... name) {
        Arrays.asList(name).forEach(mIgnore::add);
    }

    public void notice(String... name) {
        Arrays.asList(name).forEach(mNotice::add);
    }
}
