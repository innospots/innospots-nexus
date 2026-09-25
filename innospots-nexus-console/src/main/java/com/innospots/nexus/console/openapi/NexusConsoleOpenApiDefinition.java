package com.innospots.nexus.console.openapi;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.core.openapi.NexusOpenApiSecurityNames;

/**
 * 控制台 OpenAPI 全局元数据（构建期扫描；非 JAX-RS {@code Application}，以便与 portal/platform 共宿主）。
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Innospots Nexus Console API",
                version = "1.0.0",
                description = "管理控制台 Jakarta REST 契约；运行时仅暴露构建期生成的 OpenAPI。",
                contact = @Contact(name = "Innospots Nexus")
        ),
        tags = {
                @Tag(name = "Console", description = "控制台状态与健康"),
                @Tag(name = "Role", description = "角色与绑定"),
                @Tag(name = "Permission", description = "授权与可见资源"),
                @Tag(name = "Catalog", description = "控制台目录索引"),
                @Tag(name = "Navigation", description = "运行时导航"),
                @Tag(name = "Dictionary", description = "租户级字典"),
                @Tag(name = "Plugin", description = "插件生命周期"),
                @Tag(name = "OpenApiCatalog", description = "OpenAPI 规范目录")
        }
)
@SecurityScheme(
        securitySchemeName = NexusOpenApiSecurityNames.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "紧凑访问令牌（IDENTITY 或 BUSINESS）"
)
public final class NexusConsoleOpenApiDefinition {
    private NexusConsoleOpenApiDefinition() {
    }
}
