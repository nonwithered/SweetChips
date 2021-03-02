package org.sweetchips.transformlauncher.bridge;

import java.io.File;
import java.util.Map;

public interface DirectoryInput extends QualifiedContent {

    Map<File, Status> getChangedFiles();
}
