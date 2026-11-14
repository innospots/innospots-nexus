package com.innospots.nexus.console.scope.tenant.domain.enums;

/**
 * 平台管理租户的生命周期状态。
 *
 * <p>状态的切换由运营平台按需发起，不做强制状态机校验：任意合法状态都可作为
 * 目标状态写入。{@link #DISSOLVED} 表示团队已解散，可再次切换回
 * {@link #ACTIVE} 恢复使用。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity
 */
public enum TenantStatus {

    /**
     * 租户已启用，可正常访问。
     */
    ACTIVE,

    /**
     * 租户已开通但尚未启用，暂不可访问。
     */
    PENDING,

    /**
     * 租户已停用，访问被临时阻断，可再次启用。
     */
    DISABLED,

    /**
     * 租户已解散（团队注销），数据保留，可重新启用。
     */
    DISSOLVED
}
