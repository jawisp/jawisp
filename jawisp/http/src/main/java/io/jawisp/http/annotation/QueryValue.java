package io.jawisp.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom @PathVariable annotation for your Micronaut controllers.
 * Binds method parameter exclusively from URI path variable.
 * 
 * Optional if parameter name matches URI variable {id}.
 * Required when names differ: @PathVariable("userId") Long id
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryValue {
    String value();  // MANDATORY: maps to query param name (@QueryValue("q") String query)
    String defaultValue() default "";  // OPTIONAL: default when missing
    boolean required() default false;  // OPTIONAL: throw if missing
}
