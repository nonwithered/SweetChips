package org.sweetchips.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.CONSTRUCTOR})
public @interface Hide {
}