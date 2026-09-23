package com.innospots.nexus.service.contract.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记显式未认证入口。与同方法非空权限冲突将导致启动失败。
 *
 * @author Smars
 * @date 2026/09/13
 * @see RequiresPermission
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PublicAccess {
}
