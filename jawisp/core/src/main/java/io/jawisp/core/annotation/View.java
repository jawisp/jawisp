package io.jawisp.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// View annotation to specify view templates for controllers
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface View {
    String value() default "";
}
