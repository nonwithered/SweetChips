package org.sweetchips.plugin4gradle.hook;

import java.io.File;
import java.util.Map;

public interface DirectoryInput extends QualifiedContent {

    Map<File, Status> getChangedFiles();
}
