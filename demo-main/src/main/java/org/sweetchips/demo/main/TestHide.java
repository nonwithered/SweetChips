package org.sweetchips.demo.main;

import org.sweetchips.annotations.Hide;

import java.lang.reflect.Member;
import java.util.Arrays;

@Hide
final class TestHide extends AbstractTest {

    private static final int SYNTHETIC = 0x00001000;

    @Hide
    @Override
    protected final void onTest() {
        log("type", check(null, null));
        log("field", check("mObj", null));
        log("method", check("onTest", new Class[]{}));
        log("constructor", check("<init>", new Class[]{Object.class}));
    }

    @Hide
    private final Object mObj;

    @Hide
    TestHide(Object obj) {
        super();
        mObj = obj;
    }

    TestHide() {
        this(null);
    }

    private boolean check(String name, Class<?>[] types) {
        return check(SYNTHETIC, TestHide.class, name, types);
    }

    private boolean check(int flags, Class<?> clazz, String name, Class<?>[] types) {
        try {
            if (name == null) {
                if (types != null) {
                    throw new IllegalArgumentException(Arrays.toString(types));
                }
                return check(flags, clazz.getModifiers());
            }
            Member member = types == null
                    ? clazz.getDeclaredField(name)
                    : name.equals("<init>")
                    ? clazz.getDeclaredConstructor(types)
                    : clazz.getDeclaredMethod(name, types);
            return check(flags, member.getModifiers());
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private boolean check(int flags, int modifiers) {
        return (modifiers & flags) == flags;
    }
}
