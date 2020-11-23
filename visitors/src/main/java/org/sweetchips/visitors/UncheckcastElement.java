package org.sweetchips.visitors;

import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.HashSet;

class UncheckcastElement {

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

    UncheckcastElement(String name, String desc) {
        mName = name;
        mDesc = desc;
    }

    @Override
    public int hashCode() {
        return mName.hashCode() ^ mDesc.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UncheckcastElement)) {
            return false;
        }
        UncheckcastElement uncheckcastElement = (UncheckcastElement) object;
        return mName.equals(uncheckcastElement.mName) && mDesc.equals(uncheckcastElement.mDesc);
    }
}
