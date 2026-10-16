package com.innospots.nexus.platform.organization.domain.enums;

/**
 * 平台侧登记的租户管理员角色（持久化于 {@code nx_pl_tenant_admin.admin_role}）。
 */
public enum TenantAdminRole {

    /**
     * 所有者（可同步至 {@code nx_pl_tenant.owner_tenant_user_id}）。
     */
    OWNER,

    /**
     * 管理员（运营登记，无所有者同等语义时不写 owner 列）。
     */
    ADMINISTRATOR
}
