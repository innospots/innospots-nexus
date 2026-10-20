package com.innospots.nexus.console.endpoint.grant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明 REST 契约归属的控制台模块（与 entry 插件 {@code moduleKey} 一致）。
 *
 * <p>标注于 console 侧 endpoint 接口；实现类通过 implements 继承模块级
 * {@link #ignoreAuth()} / {@link #ignorePermission()} 默认值。</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EndpointModule {

    /**
     * 模块键，与 {@code ConsoleModuleDescriptor.moduleKey()} 对齐。
     */
    String value();

    /**
     * 模块内全部方法默认跳过权限校验（方法级 {@link EndpointGrant} 可再置 true）。
     */
    boolean ignorePermission() default false;

    /**
     * 模块内全部方法默认跳过登录鉴权（方法级 {@link EndpointGrant} 可再置 true）。
     */
    boolean ignoreAuth() default false;
}
