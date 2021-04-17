package org.sweetchips.demo.main;

import java.lang.reflect.Field;

final class TestConst extends AbstractTest {

    private static final long serialVersionUID = -1;

    @Override
    protected final void onTest() {
        checkSave();
        checkIgnore();
        checkNotice();
    }

    private void checkSave() {
        log("checkSave", getField(TestConst.class, "serialVersionUID").equals(serialVersionUID));
    }

    private void checkIgnore() {
        log("checkIgnore", "ignore".equals(getField(CheckInternal.class, "sIgnore")));
    }

    private void checkNotice() {
        log("checkNotice", RuntimeException.class == getField(CheckInternal.class, "sNotice").getClass());
    }
    private interface CheckInternal {

        String sIgnore = "ignore";
        String sNotice = "notice";
    }

    private Object getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            return field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return new RuntimeException(e);
        }
    }
}
