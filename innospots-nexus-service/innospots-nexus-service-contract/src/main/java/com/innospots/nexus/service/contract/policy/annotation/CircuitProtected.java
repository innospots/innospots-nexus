package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies a named circuit-breaker policy. {@code value} is a configuration key.
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.governance.CircuitBreakerProvider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CircuitProtected {

    /**
     * Circuit-breaker policy key.
     *
     * @return policy key
     */
    String value();
}
