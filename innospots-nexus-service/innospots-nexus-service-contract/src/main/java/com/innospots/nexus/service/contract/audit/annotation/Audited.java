package com.innospots.nexus.service.contract.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.innospots.nexus.service.contract.policy.AuditMode;

/**
 * Declares an audit action on an application service method. Place on Application Service, not Domain.
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.audit.AuditEvent
 * @see AuditMode
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Audited {

    /**
     * Audit action name.
     *
     * @return action
     */
    String action();

    /**
     * Resource type. Empty means the resource resolver must supply it at startup.
     *
     * @return resource type
     */
    String resourceType() default "";

    /**
     * Snapshot provider key. Empty means empty before/after maps.
     *
     * @return snapshot key
     */
    String snapshot() default "";

    /**
     * Persistence mode.
     *
     * @return audit mode
     */
    AuditMode mode() default AuditMode.BEST_EFFORT;
}
