package com.innospots.nexus.core.server.domain.entity;

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
 * Persistence entity for a registered service instance.
 * Platform-wide infrastructure data, not tenant-scoped.
 */
@Getter
@Setter
@Entity
@Table(name = ServiceRegistryEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_service_registry_instance", columnList = "instance_id", unique = true),
        @Index(name = "idx_nx_service_registry_name_service_status", columnList = "service_name,service_status")
})
@TableName(ServiceRegistryEntity.TABLE_NAME)
public class ServiceRegistryEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_service_registry";

    /**
     * Service registry identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String serviceRegistryId;

    @Override
    public String idPrefix() {
        return "srv";
    }

    /**
     * Logical service type name.
     */
    @Column(length = 128)
    private String serviceName;

    /**
     * Unique instance identifier.
     */
    @Column(length = 128)
    private String instanceId;

    /**
     * Host address.
     */
    @Column(length = 256)
    private String host;

    /**
     * Listen port.
     */
    @Column
    private Integer port;

    /**
     * Service lifecycle status.
     */
    @Column(length = 32)
    private String serviceStatus;

    /**
     * Cluster role of this service instance. Not named {@code role} because it
     * is a SQL reserved word in some dialects.
     */
    @Column(length = 32)
    private String serviceRole;

    /**
     * Service group name. Not named {@code group} because it is a SQL reserved word.
     */
    @Column(length = 64)
    private String groupName;

    /**
     * Serialized tags.
     */
    @Column(length = 512)
    private String tags;

    /**
     * Serialized runtime metrics.
     */
    @Column(length = 1024)
    private String metrics;
}
