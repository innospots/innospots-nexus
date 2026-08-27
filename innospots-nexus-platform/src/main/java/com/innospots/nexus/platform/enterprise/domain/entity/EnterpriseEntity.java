package com.innospots.nexus.platform.enterprise.domain.entity;

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

/**
 * Ops-side legal enterprise profile, one-to-one with {@code nx_tenant}.
 */
@Getter
@Setter
@Entity
@Table(name = EnterpriseEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_enterprise_tenant", columnList = "tenant_id", unique = true)
})
@TableName(EnterpriseEntity.TABLE_NAME)
public class EnterpriseEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_enterprise";

    /**
     * Enterprise identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String enterpriseId;

    @Override
    public String idPrefix() {
        return "ent";
    }

    /**
     * Tenant this profile belongs to.
     */
    @Column(length = 32, nullable = false)
    private String tenantId;

    /**
     * Legal registered name.
     */
    @Column(length = 256, nullable = false)
    private String legalName;

    /**
     * Unified social credit code or equivalent identifier.
     */
    @Column(length = 64)
    private String creditCode;

    /**
     * Industry classification.
     */
    @Column(length = 64)
    private String industry;

    /**
     * Primary contact name.
     */
    @Column(length = 128)
    private String contactName;

    /**
     * Primary contact phone.
     */
    @Column(length = 32)
    private String contactPhone;

    /**
     * Primary contact email.
     */
    @Column(length = 128)
    private String contactEmail;

    /**
     * Registered or mailing address.
     */
    @Column(length = 512)
    private String address;

    /**
     * Extensible JSON or free-form attributes.
     */
    @Column(length = 1024)
    private String extra;
}
