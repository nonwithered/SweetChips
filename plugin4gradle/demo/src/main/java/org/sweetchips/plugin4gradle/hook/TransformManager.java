package org.sweetchips.plugin4gradle.hook;

import java.util.Collections;
import java.util.Set;

public abstract class TransformManager {

    @SuppressWarnings("unchecked")
    public static final Set<QualifiedContent.ContentType> CONTENT_CLASS = Collections.EMPTY_SET;

    @SuppressWarnings("unchecked")
    public static final Set<QualifiedContent.Scope> SCOPE_FULL_PROJECT = Collections.EMPTY_SET;
}
