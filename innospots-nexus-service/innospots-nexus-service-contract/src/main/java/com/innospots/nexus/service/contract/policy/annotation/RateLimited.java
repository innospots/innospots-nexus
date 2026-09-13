package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies a named rate-limit policy. {@code value} is a configuration key, not a numeric limit.
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.governance.RateLimitProvider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RateLimited {

    /**
     * Rate-limit policy key.
     *
     * @return policy key
     */
    String value();
}
