package com.innospots.nexus.base.domain.scope;

import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;

/**
 * 租户身份及其业务组织档案（console 作用域端口与 kernel 实现共享）。
 */
public record TenantScope(TenantSnapshot tenant, OrganizationSnapshot organization) {
}
