package com.innospots.nexus.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA/MyBatis-Plus entity for service discovery registry.
 * <p>Maps to the {@code nexus_service_registry} table and inherits audit
 * fields from {@link BaseEntity} (not project-scoped, as services are
 * infrastructure-level).</p>
 */
@Getter
@Setter
@Entity
@Table(name = ServiceRegistryEntity.TABLE_NAME)
@TableName(ServiceRegistryEntity.TABLE_NAME)
public class ServiceRegistryEntity extends BaseEntity {

    public static final String TABLE_NAME = "nexus_service_registry";

    @TableId(type = IdType.INPUT)
    @Id
    @Column(length = 32, nullable = false)
    private String serverId;
    @Column(length = 128)
    private String serviceName;
    @Column(length = 128)
    private String instanceId;
    @Column(length = 256)
    private String host;
    @Column
    private Integer port;
    @Column(length = 32)
    private String serviceStatus;
    @Column(length = 32)
    private String serviceRole;
    @Column(length = 64)
    private String groupName;
    @Column(length = 512)
    private String tags;
    @Column(length = 1024)
    private String metrics;
}
