package com.innospots.nexus.service.contract.security;

import com.innospots.nexus.base.util.Checks;

/**
 * 授权决策。拒绝时携带非敏感原因码。
 *
 * @param allowed    是否允许访问
 * @param reasonCode 允许时为空
 * @author Smars
 * @date 2026/09/13
 * @see PermissionProvider
 */
public record PermissionDecision(boolean allowed, String reasonCode) {

    public PermissionDecision {
        if (allowed) {
            reasonCode = reasonCode == null ? "" : reasonCode;
        } else {
            Checks.notBlank(reasonCode, "reasonCode");
        }
    }

    /**
     * 返回允许决策。
     *
     * @return 允许
     */
    public static PermissionDecision allow() {
        return new PermissionDecision(true, "");
    }

    /**
     * 返回带非敏感原因码的拒绝决策。
     *
     * @param reasonCode 对外映射为 NO_PERMISSION 的原因码
     * @return 拒绝
     */
    public static PermissionDecision deny(String reasonCode) {
        return new PermissionDecision(false, reasonCode);
    }
}
