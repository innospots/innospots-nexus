package com.innospots.nexus.console.permission.pageaccess;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 页面访问校验结果。
 */
@Schema(name = "PageAccessVerifyResult", description = "页面访问权限校验结果")
public record PageAccessVerifyResult(
        @Schema(description = "是否允许访问", required = true)
        boolean allowed,
        @Schema(description = "拒绝原因（allowed=false 时）")
        String reason,
        @Schema(description = "每项请求的授权结果，键为 method:url")
        Map<String, Boolean> requestPermissions,
        @Schema(description = "页面模块当前主体可用的授权组")
        List<String> grantGroups,
        @Schema(description = "是否跳过页面授权的 DSL 过滤（鉴权关闭的调试状态）")
        boolean ignoreGrantGroup
) {

    public PageAccessVerifyResult {
        requestPermissions = requestPermissions == null ? Map.of() : Map.copyOf(requestPermissions);
        grantGroups = grantGroups == null ? List.of() : List.copyOf(grantGroups);
    }

    public PageAccessVerifyResult(boolean allowed, String reason) {
        this(allowed, reason, Map.of(), List.of(), false);
    }

    public static PageAccessVerifyResult allow() {
        return new PageAccessVerifyResult(true, null);
    }

    public static PageAccessVerifyResult allowIgnoringGrantGroup() {
        return new PageAccessVerifyResult(true, null, Map.of(), List.of(), true);
    }

    public static PageAccessVerifyResult deny(String reason) {
        return new PageAccessVerifyResult(false, reason);
    }
}
