package org.sweetchips.constsweeper;

import org.sweetchips.plugin4gradle.AbstractExtension;
import org.sweetchips.plugin4gradle.AbstractPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class ConstSweeperExtension extends AbstractExtension {

    private Collection<String> mIgnore = new HashSet<>();

    private Map<String, List<String>> mInterfaces = new ConcurrentHashMap<>();

    boolean unusedInterface(String name) {
        return mInterfaces.containsKey(name);
    }

    void unusedInterface(String name, String[] superNames) {
        mInterfaces.put(name, Arrays.asList(superNames));
    }

    String[] inheritedInterface(String[] interfaces) {
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

    boolean isIgnored(String clazz, String field) {
        String[] split = clazz.split("/");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            builder.append(split[i]);
            builder.append(".");
            if (mIgnore.contains(builder + "*")) {
                return true;
            }
        }
        builder.append(split[split.length - 1]);
        return mIgnore.contains(builder.toString()) || field != null && mIgnore.contains(builder + "#" + field);
    }

    public void ignore(String... name) {
        mIgnore.addAll(Arrays.asList(name));
    }

    public ConstSweeperExtension(AbstractPlugin<? extends AbstractExtension> plugin) {
        super(plugin);
    }
}
