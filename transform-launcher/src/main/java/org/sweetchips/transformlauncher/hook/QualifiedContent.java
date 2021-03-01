package org.sweetchips.transformlauncher.hook;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public interface QualifiedContent {

    interface ContentType {

        String name();

        int getValue();
    }

    interface ScopeType {

        String name();

        int getValue();
    }

    abstract class Scope implements ScopeType {
    }

    String getName();

    File getFile();

    @SuppressWarnings("unchecked")
    default Set<ContentType> getContentTypes() {
        return Collections.EMPTY_SET;
    }

    @SuppressWarnings("unchecked")
    default Set<? super Scope> getScopes() {
        return Collections.EMPTY_SET;
    }
}
