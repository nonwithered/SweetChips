package org.sweetchips.visitors;

import org.objectweb.asm.Type;
import org.sweetchips.shared.Uncheckcast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class UncheckcastRecord {

    static final String NAME = "L" + Uncheckcast.class.getName().replace(".", "/") + ";";

    private static final Map<String, Map<UncheckcastRecord, UncheckcastRecord>> sTargets = new ConcurrentHashMap<>();

    static Map<String, Map<UncheckcastRecord, UncheckcastRecord>> targets() {
        return sTargets;
    }

    private final String mName;
    private final String mDesc;

    private final Collection<Type> mTypes = new HashSet<>();

    void addType(Type type) {
        mTypes.add(type);
    }

    boolean containsType(Type type) {
        return mTypes.contains(type);
    }

    boolean isEmptyTypes() {
        return mTypes.isEmpty();
    }

    UncheckcastRecord(String name, String desc) {
        mName = name;
        mDesc = desc;
    }

    @Override
    public int hashCode() {
        return mName.hashCode() ^ mDesc.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UncheckcastRecord)) {
            return false;
        }
        UncheckcastRecord record = (UncheckcastRecord) obj;
        return mName.equals(record.mName) && mDesc.equals(record.mDesc);
    }
}
