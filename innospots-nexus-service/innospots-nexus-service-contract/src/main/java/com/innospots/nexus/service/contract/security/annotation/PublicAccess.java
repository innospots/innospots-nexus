package com.innospots.nexus.service.contract.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an explicit unauthenticated entry. Conflicts with non-empty permissions on the same method
 * fail startup.
 *
 * @author Smars
 * @date 2026/09/13
 * @see RequiresPermission
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PublicAccess {
}
