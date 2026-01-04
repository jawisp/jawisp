package io.jawisp.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Secured {
    // Security rule to determine access control
    SecurityRule securityRule() default SecurityRule.IS_AUTHENTICATED;
    
    public enum SecurityRule {
        IS_ANONYMOUS,
        IS_AUTHENTICATED
    }
}
