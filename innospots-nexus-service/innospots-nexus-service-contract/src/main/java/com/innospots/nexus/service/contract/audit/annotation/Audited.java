package com.innospots.nexus.service.contract.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.innospots.nexus.service.contract.policy.AuditMode;

/**
 * 在应用服务方法上声明审计动作。应标注于 Application Service，而非 Domain。
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
     * 审计动作名称。
     *
     * @return 动作名称
     */
    String action();

    /**
     * 资源类型。为空表示启动时由资源解析器提供。
     *
     * @return 资源类型
     */
    String resourceType() default "";

    /**
     * 快照提供者键。为空表示前后映射为空。
     *
     * @return 快照键
     */
    String snapshot() default "";

    /**
     * 持久化模式。
     *
     * @return 审计模式
     */
    AuditMode mode() default AuditMode.BEST_EFFORT;
}
