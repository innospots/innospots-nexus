package com.innospots.nexus.kernel.member.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.TenantBaseEntity;
import com.innospots.nexus.kernel.member.domain.enums.TenantMemberStatus;

/**
 * Membership of a tenant user in one tenant.
 * <p>This is not a role. Authorization is expressed through role bindings
 * and permission grants.</p>
 *
 * @see TenantMemberStatus
 */
@Getter
@Setter
@Entity
@Table(name = TenantMemberEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_tenant_member_user", columnList = "tenant_id,tenant_user_id", unique = true)
})
@TableName(TenantMemberEntity.TABLE_NAME)
public class TenantMemberEntity extends TenantBaseEntity {

    public static final String TABLE_NAME = "nx_tenant_member";

    /**
     * Membership identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String tenantMemberId;

    @Override
    public String idPrefix() {
        return "tmb";
    }

    /**
     * Tenant-realm user identity.
     */
    @Column(length = 32, nullable = false)
    private String tenantUserId;

    /**
     * Membership status persisted as {@link TenantMemberStatus} name.
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * Time the membership became effective.
     */
    @Column(nullable = false)
    private LocalDateTime joinedAt;
}
