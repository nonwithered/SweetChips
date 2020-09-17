package org.sweetchips.visitors;

import java.util.Arrays;

class Elements {

    private final Object[] mObjects;

    Elements(Object... objects) {
        mObjects = objects;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Object object : mObjects) {
            hash ^= object.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Elements)) {
            return false;
        }
        return Arrays.equals(mObjects, ((Elements) object).mObjects);
    }

}
