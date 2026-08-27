package com.innospots.nexus.console.auth.domain.model;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * Realm-neutral user identity returned by a user directory port.
 *
 * @param userId    platform_user_id or tenant_user_id
 * @param loginName login name, email, or mobile that matched
 * @param status    lifecycle status name
 * @param realm     owning security realm
 */
public record AuthUser(String userId, String loginName, String status, SecurityRealm realm) {
}
