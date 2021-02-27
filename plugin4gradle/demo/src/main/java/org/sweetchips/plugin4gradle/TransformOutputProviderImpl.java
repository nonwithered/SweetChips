package org.sweetchips.plugin4gradle;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformOutputProvider;

import java.io.File;
import java.io.IOException;
import java.util.Set;

final class TransformOutputProviderImpl implements TransformOutputProvider {

    @Override
    public void deleteAll() throws IOException {
    }

    @Override
    public File getContentLocation(String name, Set<QualifiedContent.ContentType> types, Set<? super QualifiedContent.Scope> scopes, Format format) {
        return null;
    }
}
