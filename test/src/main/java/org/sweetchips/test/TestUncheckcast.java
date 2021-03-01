package org.sweetchips.test;

import org.sweetchips.shared.Uncheckcast;

final class TestUncheckcast extends AbstractTest {

    @Override
    protected void onTest() {
        checkMethod();
        checkType();
    }

    @Uncheckcast({String.class})
    private void checkMethod() {
        boolean check;
        Object obj = new Object();
        try {
            String str = (String) obj;
            check = true;
        } catch (ClassCastException e) {
            check = false;
        }
        log("method_uncheckcast", check);
        try {
            Error err = (Error) obj;
            check = false;
        } catch (ClassCastException e) {
            check = true;
        }
        log("method_checkcast", check);
    }

    private void checkType() {
        new CheckType().check();
    }
    @Uncheckcast({String.class})
    private final class CheckType {

        private void check() {
            boolean check;
            Object obj = new Object();
            try {
                String str = (String) obj;
                check = true;
            } catch (ClassCastException e) {
                check = false;
            }
            log("type_uncheckcast", check);
            try {
                Error err = (Error) obj;
                check = false;
            } catch (ClassCastException e) {
                check = true;
            }
            log("type_checkcast", check);
        }
    }
}
