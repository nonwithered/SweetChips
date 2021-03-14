package org.sweetchips.demo.main;

import java.lang.reflect.Field;

final class TestConst extends AbstractTest {

    private static final String sConstant = "constant";

    @Override
    protected void onTest() {
        checkConstant();
        checkIgnore();
        checkNotice();
    }

    private void checkConstant() {
        log("checkConstant", RuntimeException.class == getField(TestConst.class, "sConstant").getClass());
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
