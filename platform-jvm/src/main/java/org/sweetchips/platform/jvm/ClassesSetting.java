package org.sweetchips.platform.jvm;

public final class ClassesSetting {

    private ClassesSetting() {
        throw new UnsupportedOperationException();
    }

    private static final ThreadLocal<ThreadLocal<?>> sDeleteFlag = new ThreadLocal<>();

    static boolean checkDeleteFlag() {
        if (sDeleteFlag.get() == null) {
            return false;
        } else {
            sDeleteFlag.remove();
            return true;
        }
    }

    public static void deleteCurrentClass() {
        sDeleteFlag.set(sDeleteFlag);
    }
}
