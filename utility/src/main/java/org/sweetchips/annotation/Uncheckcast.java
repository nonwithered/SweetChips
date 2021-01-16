package org.sweetchips.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE,
        ElementType.METHOD,
        ElementType.CONSTRUCTOR})
public @interface Uncheckcast {

    Class<?>[] value() default {};
}



