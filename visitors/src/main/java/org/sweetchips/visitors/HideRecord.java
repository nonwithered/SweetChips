package org.sweetchips.visitors;

import org.sweetchips.shared.Hide;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class HideRecord {

    static final String NAME = "L" + Hide .class.getName().replace(".", "/") + ";";

    private static final Map<String, Collection<HideRecord>> sTargets = new ConcurrentHashMap<>();

    static Map<String, Collection<HideRecord>> targets() {
        return sTargets;
    }

    private final String mName;
    private final String mDesc;

    HideRecord(String name, String desc) {
        mName = name;
        mDesc = desc;
    }

    @Override
    public int hashCode() {
        return mName.hashCode() ^ mDesc.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HideRecord)) {
            return false;
        }
        HideRecord record = (HideRecord) obj;
        return mName.equals(record.mName) && mDesc.equals(record.mDesc);
    }
}
