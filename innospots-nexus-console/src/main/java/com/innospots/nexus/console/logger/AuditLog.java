package com.innospots.nexus.console.logger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记方法为审计日志操作。
 * <p>框架无关标记：本包自身不执行拦截。
 * 拦截器适配器（AspectJ、Byte Buddy、CDI 等）位于包外；
 * portal 读取本注解以驱动共享
 * {@link LogExecutor} 例程。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 被审计操作的可读描述。
     */
    String value() default "";

    /**
     * 审计表中记录的稳定业务动作编码。
     */
    String action() default "";

    /**
     * 是否将方法参数作为关键参数捕获。
     */
    boolean recordArgs() default true;

    /**
     * 是否捕获方法返回值。
     */
    boolean recordResult() default false;

    /**
     * 是否捕获抛出的异常。
     */
    boolean recordException() default true;
}
