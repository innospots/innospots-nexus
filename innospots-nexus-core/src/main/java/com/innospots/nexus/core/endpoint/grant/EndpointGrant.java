package com.innospots.nexus.core.endpoint.grant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.innospots.nexus.core.endpoint.grant.I18nLiteral;

/**
 * 单条 REST 方法的授权、审计与跳过策略（平台 REST 契约单一事实源）。
 *
 * <p>宿主 JAX-RS 过滤器通过 {@link com.innospots.nexus.core.endpoint.grant.support.EndpointGrantAnnotations}
 * 解析；platform/portal 实现类不必重复声明。</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EndpointGrant {

    /**
     * 动作类型，见 {@link EndpointGrantAction}。
     */
    EndpointGrantAction action();

    /**
     * 稳定业务名，用于权限资源后缀与审计“操作对象”（如 {@code dictionary.type.create}）。
     */
    String name();

    /**
     * 审计落库时的多语言操作说明；仅 {@link #auditLog()} 为 true 时使用。
     */
    I18nLiteral label() default @I18nLiteral;

    /**
     * 审计时是否记录请求参数。
     */
    boolean recordArgs() default true;

    /**
     * 是否写入审计日志。
     */
    boolean auditLog() default false;

    /**
     * 跳过权限校验（与 {@link EndpointModule#ignorePermission()} 为 OR 关系）。
     */
    boolean ignorePermission() default false;

    /**
     * 跳过登录鉴权（与 {@link EndpointModule#ignoreAuth()} 为 OR 关系）。
     */
    boolean ignoreAuth() default false;

    /**
     * READ/EXPORT：目标 {@code grantName}；可变更类：{@code operationGroups.group}（缺省 {@code default}）。
     */
    String group() default "";
}
