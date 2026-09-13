package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies a named timeout policy. {@code value} is a configuration key, not a {@link java.time.Duration}.
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.time.Deadline
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TimeoutProtected {

    /**
     * Timeout policy key.
     *
     * @return policy key
     */
    String value();
}
