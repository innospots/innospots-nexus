package com.innospots.nexus.console.logger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 标记方法为审计日志操作。
 * <p>框架无关标记：本包自身不执行拦截。
 * 宿主的 JAX-RS 审计过滤器与 {@link LogExecutor} 读取本注解，
 * 通过调用日志处理器落库；REST 契约可在接口方法上声明本注解。</p>
 * <p>操作描述使用嵌套 {@link I18nLiteral}（运行时转为 {@link I18nObject}），
 * 按 {@link com.innospots.nexus.base.i18n.I18nConverter} 当前语言环境写入
 * {@code nx_audit_log.message}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 被审计操作的多语言描述。
     */
    I18nLiteral value() default @I18nLiteral;

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
