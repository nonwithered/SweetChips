package org.sweetchips.plugin4gradle;

import java.nio.file.Path;
import java.util.function.BiConsumer;

interface Util {

    String NAME = "SweetChips";

    ThreadLocal<Boolean> CLASS_UNUSED = new ThreadLocal<>();

    ThreadLocal<BiConsumer<Path, byte[]>> CLASS_CREATE = new ThreadLocal<>();

    static boolean ignoreFile(String name) {
        return !name.endsWith(".class")
                || name.startsWith("R$")
                || name.equals("R.class");
    }

    String DO_FIRST = "doFirst";

    String DO_LAST = "doLast";
}
