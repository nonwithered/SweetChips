package org.sweetchips.visitors;

class HideElement {

    private final String mName;
    private final String mDesc;

    HideElement(String name, String desc) {
        mName = name;
        mDesc = desc;
    }

    @Override
    public int hashCode() {
        return mName.hashCode() ^ mDesc.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HideElement)) {
            return false;
        }
        HideElement hideElement = (HideElement) object;
        return mName.equals(hideElement.mName) && mDesc.equals(hideElement.mDesc);
    }
}
