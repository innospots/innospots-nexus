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
 * 已注册服务实例的持久化实体；平台级基础设施数据，不按租户隔离。
 *
 * @author Smars
 * @date 2026/09/13
 * @see BaseEntity
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

    /** 服务注册标识。 */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String serviceRegistryId;

    @Override
    public String idPrefix() {
        return "srv";
    }

    /** 逻辑服务类型名称。 */
    @Column(length = 128)
    private String serviceName;

    /** 唯一实例标识。 */
    @Column(length = 128)
    private String instanceId;

    /** 主机地址。 */
    @Column(length = 256)
    private String host;

    /** 监听端口。 */
    @Column
    private Integer port;

    /** 服务生命周期状态。 */
    @Column(length = 32)
    private String serviceStatus;

    /**
     * 该服务实例的集群角色。未命名为 {@code role}，因部分 SQL 方言中为保留字。
     */
    @Column(length = 32)
    private String serviceRole;

    /**
     * 服务分组名称。未命名为 {@code group}，因 SQL 保留字。
     */
    @Column(length = 64)
    private String groupName;

    /** 序列化后的标签。 */
    @Column(length = 512)
    private String tags;

    /** 序列化后的运行时指标。 */
    @Column(length = 1024)
    private String metrics;
}
