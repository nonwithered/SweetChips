package org.sweetchips.platform.jvm;

public final class ClassesSetting {

    private ClassesSetting() {
        throw new UnsupportedOperationException();
    }

    private static final ThreadLocal<Boolean> sDeleteFlag = new ThreadLocal<>();

    static void resetDeleteFlag() {
        if (sDeleteFlag.get() != Boolean.FALSE) {
            sDeleteFlag.set(Boolean.FALSE);
        }
    }

    static boolean checkDeleteFlag() {
        return sDeleteFlag.get() == Boolean.TRUE;
    }

    public static void deleteCurrentClass() {
        sDeleteFlag.set(Boolean.TRUE);
    }
}
