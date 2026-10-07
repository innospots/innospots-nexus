package com.innospots.nexus.console.credential.ownership;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.ConsoleOwnership;

/**
 * 将 {@link com.innospots.nexus.console.auth.domain.enums.SecurityRealm} 映射为凭据/OTP 行的
 * {@link com.innospots.nexus.console.scope.ConsoleOwnership}（身份级 TENANT/PLATFORM 时 {@code ownerId} 可为 null）。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.credential.password.operator.UserCredentialOperator
 */
public final class CredentialOwnershipResolver {

    private CredentialOwnershipResolver() {
    }

    /**
     * 平台或租户身份域的默认归属（身份级；{@code ownerId} 在尚无租户上下文时为 null）。
     * <p>调用场景：TOTP 注册、租户级密码凭据写入 {@link com.innospots.nexus.console.credential.password.operator.UserCredentialOperator} 时。</p>
     *
     * @param realm 平台或租户安全域
     * @return 对应 {@link ConsoleOwnership}，身份级时 {@code ownerId} 为 null
     */
    public static ConsoleOwnership ownershipForRealm(SecurityRealm realm) {
        Checks.notNull(realm, "realm");
        if (realm == SecurityRealm.PLATFORM) {
            return new ConsoleOwnership(RoleOwnerType.PLATFORM, null, SecurityRealm.PLATFORM.name());
        }
        return new ConsoleOwnership(RoleOwnerType.TENANT, null, SecurityRealm.TENANT.name());
    }

    /**
     * 租户业务资源下的凭据归属（例如按租户隔离的扩展凭据）。
     * <p>调用场景：工作区/租户资源级凭据扩展，需显式 {@code tenantId} 隔离时。</p>
     *
     * @param tenantId 租户主键
     * @return 带 {@code ownerId} 的租户归属
     */
    public static ConsoleOwnership tenantResourceOwnership(String tenantId) {
        Checks.notBlank(tenantId, "tenantId");
        return new ConsoleOwnership(RoleOwnerType.TENANT, tenantId, SecurityRealm.TENANT.name());
    }
}
