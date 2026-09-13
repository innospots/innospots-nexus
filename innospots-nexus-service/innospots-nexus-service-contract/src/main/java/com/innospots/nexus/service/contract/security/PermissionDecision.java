package com.innospots.nexus.service.contract.security;

import com.innospots.nexus.base.util.Checks;

/**
 * Authorization decision. Denials carry a non-sensitive reason code.
 *
 * @param allowed    whether access is granted
 * @param reasonCode empty when allowed
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
     * Returns an allow decision.
     *
     * @return allow
     */
    public static PermissionDecision allow() {
        return new PermissionDecision(true, "");
    }

    /**
     * Returns a deny decision with a non-sensitive reason code.
     *
     * @param reasonCode reason code mapped to NO_PERMISSION externally
     * @return deny
     */
    public static PermissionDecision deny(String reasonCode) {
        return new PermissionDecision(false, reasonCode);
    }
}
