package org.sweetchips.constsweeper;

import org.sweetchips.plugin4gradle.AbstractExtension;
import org.sweetchips.plugin4gradle.AbstractPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class ConstSweeperExtension extends AbstractExtension {

    private MemberScope mIgnore = newMemberScope();

    private MemberScope mNotice = newMemberScope();

    private Map<String, List<String>> mInterfaces = new ConcurrentHashMap<>();

    boolean unusedInterface(String name) {
        return mInterfaces.containsKey(name);
    }

    void unusedInterface(String name, String[] superNames) {
        mInterfaces.put(name, Arrays.asList(superNames));
    }

    String[] inheritedInterface(String[] interfaces) {
        if (interfaces == null) {
            return new String[0];
        }
        List<String> list = new ArrayList<>();
        Queue<String> queue = new LinkedList<>(Arrays.asList(interfaces));
        String name;
        while ((name = queue.poll()) != null) {
            if (!mInterfaces.containsKey(name)) {
                list.add(name);
                continue;
            }
            List<String> supers = mInterfaces.get(name);
            if (supers != null) {
                queue.addAll(supers);
            }
        }
        String[] array = list.toArray(new String[0]);
        return array.length > 0 ? array : null;
    }

    boolean isIgnored(String clazz, String member) {
        return mIgnore.contains(clazz, member) && !mNotice.contains(clazz, member);
    }

    public void ignore(String... name) {
        Arrays.asList(name).forEach(mIgnore::add);
    }

    public void notice(String... name) {
        Arrays.asList(name).forEach(mNotice::add);
    }

    public ConstSweeperExtension(AbstractPlugin<? extends AbstractExtension> plugin) {
        super(plugin);
    }
}
