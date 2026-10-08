package com.innospots.nexus.spring.core.jaxrs;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 显式启用 Nexus 宿主无关的 Jakarta REST 装配。
 *
 * <p>聚合本包四个 Configuration：Jersey 资源注册（{@link NexusJaxRsConfiguration}）、
 * 通用异常映射（{@link NexusJaxRsExceptionConfiguration}）、OpenAPI 规范目录
 * （{@link OpenApiCatalogConfiguration}）与 Scalar 文档页
 * （{@link NexusScalarJerseyConfiguration}）；均与权限、跨域、console/platform 业务域无关。
 * console 专属过滤器装配见 {@code com.innospots.nexus.spring.console.ConsoleJaxRsWebConfiguration}。</p>
 *
 * @see NexusJaxRsConfiguration
 * @see NexusJaxRsExceptionConfiguration
 * @see OpenApiCatalogConfiguration
 * @see NexusScalarJerseyConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        NexusJaxRsConfiguration.class,
        NexusJaxRsExceptionConfiguration.class,
        OpenApiCatalogConfiguration.class,
        NexusScalarJerseyConfiguration.class
})
public @interface EnableNexusJaxRs {
}
