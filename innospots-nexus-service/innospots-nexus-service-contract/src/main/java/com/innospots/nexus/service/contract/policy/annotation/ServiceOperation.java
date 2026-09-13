package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a stable operation identifier on a method. Absent annotations derive ids from route plus method.
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.policy.OperationDescriptor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceOperation {

    /**
     * Stable operation identifier.
     *
     * @return operation id
     */
    String value();
}
