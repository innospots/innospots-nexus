package com.innospots.nexus.core.endpoint.grant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明 REST 契约归属的控制台模块（与 entry 插件 {@code moduleKey} 一致）。
 *
 * <p>标注于 JAX-RS 资源实现类；console 契约接口仅声明 {@link EndpointGrant}。
 * 实现类通过 {@code domain()} 与 catalog {@code domainKey} 对齐。</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EndpointModule {

    /**
     * 模块键，与 console@2 MODULE {@code moduleKey} 对齐。
     */
    String value();

    /**
     * 领域键，与 catalog MODULE {@code domainKey} 对齐，用于拼装
     * {@code domainKey.moduleKey:grantName} 资源标识；未声明时由宿主按 catalog 解析。
     */
    String domain() default "";

    /**
     * 模块内全部方法默认跳过权限校验（方法级 {@link EndpointGrant} 可再置 true）。
     */
    boolean ignorePermission() default false;

    /**
     * 模块内全部方法默认跳过登录鉴权（方法级 {@link EndpointGrant} 可再置 true）。
     */
    boolean ignoreAuth() default false;
}
