package org.sweetchips.plugin4gradle;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public abstract class AbstractExtension {

    private final AbstractPlugin<? extends AbstractExtension> mPlugin;

    private boolean mInit;

    public final void attach(String task) {
        if (mInit) {
            throw new IllegalStateException();
        } else {
            mInit = true;
        }
        mPlugin.onAttach(task);
    }

    protected AbstractExtension(AbstractPlugin<? extends AbstractExtension> plugin) {
        mPlugin = plugin;
    }

    protected static MemberScope newMemberScope() {
        return new MemberScope();
    }

    protected final static class MemberScope {

        private final Collection<String> mScope;

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
            while (true) {
                if (!itr.hasNext()) {
                    break;
                }
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
