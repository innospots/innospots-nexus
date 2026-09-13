package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates an optional child span. Default request tracing does not require this annotation.
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.trace.TraceProvider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Traced {

    /**
     * Child span name.
     *
     * @return span name
     */
    String value();
}
