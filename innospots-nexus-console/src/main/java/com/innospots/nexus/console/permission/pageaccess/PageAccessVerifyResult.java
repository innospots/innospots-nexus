package com.innospots.nexus.console.permission.pageaccess;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 页面访问校验结果。
 */
@Schema(name = "PageAccessVerifyResult", description = "页面访问权限校验结果")
public record PageAccessVerifyResult(
        @Schema(description = "是否允许访问", required = true)
        boolean allowed,
        @Schema(description = "拒绝原因（allowed=false 时）")
        String reason
) {

    public static PageAccessVerifyResult allow() {
        return new PageAccessVerifyResult(true, null);
    }

    public static PageAccessVerifyResult deny(String reason) {
        return new PageAccessVerifyResult(false, reason);
    }
}
