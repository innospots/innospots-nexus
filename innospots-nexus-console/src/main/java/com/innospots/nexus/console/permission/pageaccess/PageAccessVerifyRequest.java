package com.innospots.nexus.console.permission.pageaccess;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 页面访问校验请求（catalog 页面定位 + 权限分组）。
 */
@Schema(name = "PageAccessVerifyRequest", description = "页面访问权限校验请求")
public record PageAccessVerifyRequest(
        @Schema(description = "领域键", required = true, examples = {"platform"})
        String domainKey,
        @Schema(description = "模块键", required = true, examples = {"organization"})
        String moduleKey,
        @Schema(description = "稳定入口页键", required = true)
        String pageKey,
        @Schema(description = "权限分组，缺省 default")
        String grantGroup
) {
}
