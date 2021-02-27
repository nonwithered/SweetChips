package org.sweetchips.inlinetailor;

import org.sweetchips.plugin4gradle.AbstractExtension;
import org.sweetchips.plugin4gradle.AbstractPlugin;

import java.util.Arrays;

public class InlineTailorExtension extends AbstractExtension {

    private MemberScope mIgnore = newMemberScope();

    private MemberScope mIgnoreExcept = newMemberScope();

    boolean isIgnored(String clazz, String member) {
        return mIgnore.contains(clazz, member) && !mIgnoreExcept.contains(clazz, member);
    }

    public void ignore(String... name) {
        Arrays.asList(name).forEach(mIgnore::add);
    }

    public void ignoreExcept(String... name) {
        Arrays.asList(name).forEach(mIgnoreExcept::add);
    }

    public InlineTailorExtension(AbstractPlugin<? extends AbstractExtension> plugin) {
        super(plugin);
    }
}
