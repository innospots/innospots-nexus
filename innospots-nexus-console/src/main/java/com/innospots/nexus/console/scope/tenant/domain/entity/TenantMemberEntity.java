package com.innospots.nexus.console.scope.tenant.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.console.scope.tenant.domain.entity.base.TenantBaseEntity;
import com.innospots.nexus.console.scope.tenant.domain.enums.TenantMemberStatus;

/**
 * 租户用户在单个租户中的成员关系。
 * <p>这不是角色。授权通过角色绑定与权限授权表达。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see TenantMemberStatus
 */
@Getter
@Setter
@Entity
@Table(name = TenantMemberEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_tenant_member_portal_user", columnList = "tenant_id,portal_user_id", unique = true)
})
@TableName(TenantMemberEntity.TABLE_NAME)
public class TenantMemberEntity extends TenantBaseEntity {

    public static final String TABLE_NAME = "nx_tenant_member";

    /**
     * 成员关系标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String tenantMemberId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "TMB";
    }

    /**
     * 租户域用户身份。
     */
    @Column(length = 32, nullable = false)
    private String portalUserId;

    /**
     * 以 {@link TenantMemberStatus} 名称持久化的成员关系状态。
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * 成员关系生效时间。
     */
    @Column(nullable = false)
    private LocalDateTime joinedAt;

    /**
     * 租户内的业务角色，由产品侧定义取值（如团队拥有者、管理员、成员）。
     *
     * <p>与权限体系无关：租户权限仍通过角色绑定与权限授权表达，
     * 本字段只承载产品语义上的成员角色标识，允许为空。
     * 与 {@link com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceMemberEntity#getMemberRole()}
     * 保持同一套建模方式。</p>
     */
    @Column(length = 32)
    private String memberRole;
}
