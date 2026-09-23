package com.innospots.nexus.kernel.openapi;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.core.openapi.NexusOpenApiSecurityNames;

/**
 * Kernel OpenAPI 全局元数据与 JAX-RS 应用锚点（仅用于构建期扫描）。
 */
@ApplicationPath("/")
@OpenAPIDefinition(
        info = @Info(
                title = "Innospots Nexus Kernel API",
                version = "1.0.0",
                description = "租户域认证、注册与作用域选择 Jakarta REST 契约；运行时仅暴露构建期生成的 OpenAPI。",
                contact = @Contact(name = "Innospots Nexus")
        ),
        tags = {
                @Tag(name = "TenantAuth", description = "租户域认证与注册"),
                @Tag(name = "TenantScope", description = "工作区与项目作用域选择")
        }
)
@SecurityScheme(
        securitySchemeName = NexusOpenApiSecurityNames.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "紧凑访问令牌（IDENTITY 或 BUSINESS）"
)
public class NexusKernelOpenApiDefinition extends Application {
}
