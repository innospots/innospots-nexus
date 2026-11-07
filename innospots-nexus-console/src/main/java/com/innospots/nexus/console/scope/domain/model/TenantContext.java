package com.innospots.nexus.console.scope.domain.model;

import java.util.List;

import com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity;
import com.innospots.nexus.console.scope.tenant.domain.entity.TenantMemberEntity;

/**
 * 租户上下文：租户本体、当前用户的成员关系与该成员在租户内的角色编码。
 *
 * <p>这是内部解析结果，不直接返回前端。</p>
 *
 * @param tenant    租户
 * @param member    当前用户在该租户下的成员关系
 * @param roleCodes 该成员在租户内持有的角色编码；无绑定为空列表
 * @author Smars
 * @date 2026/11/07
 */
public record TenantContext(
        TenantEntity tenant,
        TenantMemberEntity member,
        List<String> roleCodes
) {

    public TenantContext {
        roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
    }

    /**
     * 租户标识。
     *
     * @return 租户标识
     */
    public String tenantId() {
        return tenant == null ? null : tenant.getTenantId();
    }

    /**
     * 租户成员标识。
     *
     * @return 成员标识
     */
    public String tenantMemberId() {
        return member == null ? null : member.getTenantMemberId();
    }
}
