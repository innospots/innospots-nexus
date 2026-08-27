package com.innospots.nexus.console.logger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an audit-logged operation.
 * <p>Framework-independent marker: this package never performs interception
 * itself. Interceptor adapters (AspectJ, Byte Buddy, CDI, ...) live outside
 * the kernel and read this annotation to drive the shared
 * {@link LogExecutor} routine.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * Human-readable description of the audited operation.
     */
    String value() default "";

    /**
     * Stable business action code recorded in the audit table.
     */
    String action() default "";

    /**
     * Whether to capture the method arguments as key parameters.
     */
    boolean recordArgs() default true;

    /**
     * Whether to capture the method return value.
     */
    boolean recordResult() default false;

    /**
     * Whether to capture the thrown exception.
     */
    boolean recordException() default true;
}
