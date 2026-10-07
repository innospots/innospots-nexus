package com.innospots.nexus.console.permission.api;

/**
 * 校验组织单元是否属于指定租户；由 portal 或 adapter 提供实现。
 */
public interface OrganizationUnitDirectory {

    /**
     * 判断组织单元在租户下存在且可用于授权。
     *
     * @param tenantId 租户 ID
     * @param unitId   组织单元 ID
     * @return 存在且有效时为 true
     */
    boolean existsInTenant(String tenantId, String unitId);
}
