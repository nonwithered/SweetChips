package org.sweetchips.inlinetailor;

import org.sweetchips.android.AbstractExtension;

import java.util.Arrays;

public class InlineTailorExtension extends AbstractExtension {

    private MemberScope mIgnore = newMemberScope();

    private MemberScope mNotice = newMemberScope();

    boolean isIgnored(String clazz, String member) {
        return mIgnore.contains(clazz, member) && !mNotice.contains(clazz, member);
    }

    public void attach(String name) {
        InlineTailorPlugin.INSTANCE.onAttach(name);
    }

    public void ignore(String... name) {
        Arrays.asList(name).forEach(mIgnore::add);
    }

    public void notice(String... name) {
        Arrays.asList(name).forEach(mNotice::add);
    }
}
