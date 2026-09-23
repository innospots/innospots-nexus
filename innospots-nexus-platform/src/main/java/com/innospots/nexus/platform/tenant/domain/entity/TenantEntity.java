package com.innospots.nexus.platform.tenant.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.platform.tenant.domain.enums.TenantStatus;

/**
 * 平台侧拥有的租户生命周期记录。
 * <p>企业法定档案单独存储于 {@code nx_enterprise}；
 * 本表是运维侧租户身份。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see TenantStatus
 */
@Getter
@Setter
@Entity
@Table(name = TenantEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_tenant_code", columnList = "tenant_code", unique = true)
})
@TableName(TenantEntity.TABLE_NAME)
public class TenantEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_tenant";

    /**
     * 租户标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String tenantId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "tnt";
    }

    /**
     * 租户显示名称。
     */
    @Column(length = 128, nullable = false)
    private String tenantName;

    /**
     * 稳定的唯一租户编码。
     */
    @Column(length = 64, nullable = false)
    private String tenantCode;

    /**
     * 以 {@link TenantStatus} 名称持久化的生命周期状态。
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * 可选商业套餐编码。
     */
    @Column(length = 64)
    private String planCode;

    /**
     * 租户用户域的初始所有者身份。
     */
    @Column(length = 32)
    private String ownerTenantUserId;
}
