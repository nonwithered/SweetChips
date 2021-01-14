package org.sweetchips.plugin4gradle;

import java.util.Collections;
import java.util.List;

public class UnionExtension extends BaseExtension {

    private List<String> mMultiTransform = Collections.emptyList();

    public void setMultiTransform(List<String> list) {
        mMultiTransform = list;
    }

    public List<String> getMultiTransform() {
        return mMultiTransform;
    }
}
