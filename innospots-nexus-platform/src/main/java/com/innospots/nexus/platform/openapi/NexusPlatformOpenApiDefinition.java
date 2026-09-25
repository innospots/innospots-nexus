package com.innospots.nexus.platform.openapi;

import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.core.openapi.NexusOpenApiSecurityNames;

/**
 * Platform OpenAPI 全局元数据（构建期扫描；非 JAX-RS {@code Application}，以便与 console 共宿主）。
 * <p>带 {@link Path} 以便 SmallRye JAX-RS 扫描器拾取 {@link OpenAPIDefinition}，运行时不暴露端点。</p>
 */
@Path("/platform")
@OpenAPIDefinition(
        info = @Info(
                title = "Innospots Nexus Platform API",
                version = "1.0.0",
                description = "运维域租户生命周期与平台 IAM Jakarta REST 契约；运行时仅暴露构建期生成的 OpenAPI。",
                contact = @Contact(name = "Innospots Nexus")
        ),
        tags = {
                @Tag(name = "PlatformAuth", description = "运维域认证"),
                @Tag(name = "PlatformTenant", description = "租户生命周期"),
                @Tag(name = "PlatformUser", description = "平台用户管理")
        }
)
@SecurityScheme(
        securitySchemeName = NexusOpenApiSecurityNames.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "紧凑访问令牌（PLATFORM）"
)
public final class NexusPlatformOpenApiDefinition {
    private NexusPlatformOpenApiDefinition() {
    }
}
