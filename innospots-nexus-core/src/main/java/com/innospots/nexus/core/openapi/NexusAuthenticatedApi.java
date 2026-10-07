package com.innospots.nexus.core.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

/**
 * 标记需要 Bearer 认证的控制台 API 资源。
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SecurityRequirement(name = NexusOpenApiSecurityNames.BEARER_AUTH)
public @interface NexusAuthenticatedApi {
}
