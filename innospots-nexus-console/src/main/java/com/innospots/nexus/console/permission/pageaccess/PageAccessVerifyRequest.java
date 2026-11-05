package com.innospots.nexus.console.permission.pageaccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 页面访问校验请求（catalog 页面定位 + 权限分组）。
 */
@Schema(name = "PageAccessVerifyRequest", description = "页面访问权限校验请求")
public record PageAccessVerifyRequest(
        @Schema(description = "领域键，grant 模式必填", examples = {"operation"})
        String domainKey,
        @Schema(description = "模块键，grant 模式必填", examples = {"organization"})
        String moduleKey,
        @Schema(description = "稳定入口页键，grant 模式必须匹配模块入口")
        String pageKey,
        @Schema(description = "权限分组，由页面服务在缺省时填入 view")
        String grantGroup,
        @Schema(description = "页面访问模式：authenticated 或 grant", required = true)
        String accessMode,
        @Schema(description = "页面操作请求，最多 256 项")
        List<RequestItem> requests
) {
    public PageAccessVerifyRequest {
        requests = requests == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(requests));
    }

    /** 兼容只进行授权组校验的 Java 调用者。 */
    public PageAccessVerifyRequest(String domainKey, String moduleKey, String pageKey, String grantGroup) {
        this(domainKey, moduleKey, pageKey, grantGroup, "grant", List.of());
    }

    /** 页面操作所需的 HTTP 方法和 URL。 */
    public record RequestItem(String method, String url) {
    }
}
