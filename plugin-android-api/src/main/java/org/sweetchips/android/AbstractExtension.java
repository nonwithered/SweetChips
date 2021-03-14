package org.sweetchips.android;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractExtension {

    protected static MemberScope newMemberScope() {
        return new MemberScope();
    }

    protected final static class MemberScope {

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
