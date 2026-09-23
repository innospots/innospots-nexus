package com.innospots.nexus.core.resource.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.OwnershipEntity;

/**
 * 已存储文件元数据的持久化实体（按 {@link OwnershipEntity} 归属列隔离）。
 */
@Getter
@Setter
@Entity
@Table(name = MetaResourceEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_meta_resource_module", columnList = "module_key"),
        @Index(name = "idx_nx_meta_resource_uri_key", columnList = "uri_key")
})
@TableName(MetaResourceEntity.TABLE_NAME)
public class MetaResourceEntity extends OwnershipEntity {

    public static final String TABLE_NAME = "nx_meta_resource";

    /** 资源标识。 */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String resourceId;

    @Override
    public String idPrefix() {
        return "res";
    }

    /** MIME 类型。 */
    @Column(length = 128)
    private String mimeType;

    /** 文件大小（字节）。 */
    @Column
    private long fileSize;

    /** 已存储文件 URI。 */
    @Column(length = 1024)
    private String fileUri;

    /** 由 URI 派生的稳定存储键。 */
    @Column(length = 256)
    private String uriKey;

    /** 存储后端模式。 */
    @Column(length = 32)
    private String storeMode;

    /** 原始资源名称。 */
    @Column(length = 256)
    private String resourceName;

    /** 存储区域。 */
    @Column(length = 64)
    private String region;

    /** 存储后端中的目录名。 */
    @Column(length = 256)
    private String directoryName;

    /** 所属模块键。 */
    @Column(length = 128)
    private String moduleKey;

    /** 所属模块名称。 */
    @Column(length = 64)
    private String module;
}
