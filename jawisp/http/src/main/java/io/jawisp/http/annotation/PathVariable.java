package io.jawisp.http.annotation;

import java.lang.annotation.Documented;
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
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface PathVariable {
    
    /**
     * @return Name of the URI path variable to bind from.
     * Defaults to parameter name if empty.
     */
    String value() default "";
}
