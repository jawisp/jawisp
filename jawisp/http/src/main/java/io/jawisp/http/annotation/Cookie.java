package io.jawisp.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cookie {
    String value();  // MANDATORY: cookie name  
    String defaultValue() default "";
    boolean required() default false;
}
