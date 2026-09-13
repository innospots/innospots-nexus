package com.innospots.nexus.service.contract.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares required permission keys for a resource or method. Place on Controller/Resource types.
 *
 * @author Smars
 * @date 2026/09/13
 * @see PublicAccess
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiresPermission {

    /**
     * Required permission keys. Combined with type-level keys using AND.
     *
     * @return permission keys
     */
    String[] value();

    /**
     * Resource resolver key. Empty means the default resolver for the operation.
     *
     * @return resolver key
     */
    String resource() default "";
}
