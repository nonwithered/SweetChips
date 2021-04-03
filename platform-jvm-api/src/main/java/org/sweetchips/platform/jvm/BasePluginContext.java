package org.sweetchips.platform.jvm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class BasePluginContext {

    private final MemberScope mIgnore = BasePluginContext.newMemberScope();
    private final MemberScope mNotice = BasePluginContext.newMemberScope();

    public abstract void onAttach(WorkflowSettings settings);

    public boolean isIgnored(String clazz, String member) {
        return mIgnore.contains(clazz, member) && !mNotice.contains(clazz, member);
    }

    public void addIgnore(String name) {
        mIgnore.add(name);
    }

    public void addNotice(String name) {
        mNotice.add(name);
    }

    public static MemberScope newMemberScope() {
        return new MemberScope();
    }

    public static final class MemberScope {

        private final Set<String> mScope;

        MemberScope() {
            mScope = new HashSet<>();
        }

        public void add(String member) {
            mScope.add(member);
        }

        public boolean contains(String clazz, String member) {
            if (mScope.size() <= 0) {
                return false;
            }
            if (mScope.contains("*")) {
                return true;
            }
            StringBuilder builder = new StringBuilder();
            Iterator<String> itr = Arrays.asList(clazz.split("/")).iterator();
            String str = itr.next();
            while (itr.hasNext()) {
                builder.append(str);
                builder.append('.');
                builder.append('*');
                if (mScope.contains(builder.toString())) {
                    return true;
                }
                builder.setLength(builder.length() - 1);
                str = itr.next();
            }
            builder.append(str);
            return mScope.contains(builder.toString()) || member != null && mScope.contains(builder + "#" + member);
        }
    }
}
