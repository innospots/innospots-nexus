package com.innospots.nexus.console.scope.workspace.domain.entity;

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

import com.innospots.nexus.console.scope.tenant.domain.entity.base.TenantWorkspaceBaseEntity;

/**
 * 工作区成员关系（{@code nx_workspace_member}）。
 *
 * <p>把租户成员（{@link com.innospots.nexus.console.scope.tenant.domain.entity.TenantMemberEntity}）
 * 绑定到具体工作区。继承 {@link TenantWorkspaceBaseEntity}，因此
 * {@code tenantId} 与 {@code workspaceId} 两个作用域列都会从会话上下文自动填充。</p>
 *
 * <p>这不是角色：工作区内的权限仍通过角色绑定与权限授权表达。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
@Getter
@Setter
@Entity
@Table(name = WorkspaceMemberEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_workspace_member",
                columnList = "workspace_id,tenant_member_id", unique = true),
        @Index(name = "idx_nx_workspace_member_tenant", columnList = "tenant_id,tenant_member_id")
})
@TableName(WorkspaceMemberEntity.TABLE_NAME)
public class WorkspaceMemberEntity extends TenantWorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_workspace_member";

    /**
     * 工作区成员关系标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String workspaceMemberId;

    /**
     * 返回主键前缀。
     *
     * @return 固定值 {@code WSM}
     */
    @Override
    public String idPrefix() {
        return "WSM";
    }

    /**
     * 成员所属租户成员关系；指向 {@code nx_tenant_member.tenant_member_id}。
     */
    @Column(length = 32, nullable = false)
    private String tenantMemberId;

    /**
     * 成员所属租户用户；指向 {@code nx_portal_user.portal_user_id}。
     *
     * <p>冗余自租户成员关系，避免角色查询与成员展示时多一次联表。</p>
     */
    @Column(length = 32, nullable = false)
    private String portalUserId;

    /**
     * 以 {@link com.innospots.nexus.console.scope.tenant.domain.enums.TenantMemberStatus}
     * 名称持久化的成员状态。
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * 工作区内的业务角色，由产品侧定义取值（如空间管理员、策略分析）。
     *
     * <p>与权限体系无关：工作区权限仍通过角色绑定与权限授权表达，
     * 本字段只承载产品语义上的岗位标识，允许为空。</p>
     */
    @Column(length = 32)
    private String memberRole;
}
