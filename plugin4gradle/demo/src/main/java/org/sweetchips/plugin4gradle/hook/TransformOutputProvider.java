package org.sweetchips.plugin4gradle.hook;

import java.io.File;
import java.util.Set;

public interface TransformOutputProvider {

    File getContentLocation(
            String name,
            Set<QualifiedContent.ContentType> types,
            Set<? super QualifiedContent.Scope> scopes,
            Format format);
}
