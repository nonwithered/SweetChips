package org.sweetchips.test;

import java.lang.reflect.Field;

final class TestConst extends AbstractTest {

    private static final String sConst = "constant";

    @Override
    protected void onTest() {
        checkIgnoreAll();
        checkIgnorePart();
        checkIgnoreNot();
    }

    private void checkIgnoreAll() {
        log("checkIgnoreAll", "constant".equals(getField(TestConst.class, "sConst")));
    }

    private void checkIgnorePart() {
        log("checkIgnorePart_constant", RuntimeException.class == getField(CheckIgnorePart.class, "sConst").getClass());
        log("checkIgnorePart_ignore", "ignore".equals(getField(CheckIgnorePart.class, "sIgnore")));
    }
    private interface CheckIgnorePart {

        String sConst = "constant";
        String sIgnore = "ignore";
    }

    private void checkIgnoreNot() {
        boolean check;
        try {
            Class.forName("org.sweetchips.test.TestConst$CheckIgnoreNot");
            check = false;
        } catch (ClassNotFoundException e) {
            check = true;
        }
        log("checkIgnoreNot", check);
    }
    private interface CheckIgnoreNot {

        String sConst = "constant";
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
