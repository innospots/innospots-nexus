/**
 * 宿主无关的 Jakarta REST Spring 装配：Jersey 资源注册、通用异常映射、OpenAPI 目录与 Scalar 文档页。
 *
 * <p>仅承载与权限、跨域、console/platform 业务域无关的 JAX-RS 横切能力；
 * console 专属的过滤器与安全装配见 {@code com.innospots.nexus.spring.console}。</p>
 */
package com.innospots.nexus.spring.core.jaxrs;
