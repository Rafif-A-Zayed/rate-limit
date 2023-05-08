package com.addon.rateLimit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface FixedWindow {

    int limit() default 10;

    String limitExp() default "";

    //time in seconds
    long per() default 60;

    String perExp() default "";
}
