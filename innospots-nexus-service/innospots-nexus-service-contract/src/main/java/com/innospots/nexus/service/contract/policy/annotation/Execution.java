package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.innospots.nexus.service.contract.invocation.ExecutionMode;

/**
 * Declares how the annotated method may execute.
 *
 * @author Smars
 * @date 2026/09/13
 * @see ExecutionMode
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Execution {

    /**
     * Execution mode.
     *
     * @return mode
     */
    ExecutionMode value();
}
