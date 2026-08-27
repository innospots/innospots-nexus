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

import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;

/**
 * Persistence entity for stored file metadata.
 */
@Getter
@Setter
@Entity
@Table(name = MetaResourceEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_meta_resource_module", columnList = "module_key"),
        @Index(name = "idx_nx_meta_resource_uri_key", columnList = "uri_key")
})
@TableName(MetaResourceEntity.TABLE_NAME)
public class MetaResourceEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_meta_resource";

    /**
     * Resource identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String resourceId;

    @Override
    public String idPrefix() {
        return "res";
    }

    /**
     * MIME type.
     */
    @Column(length = 128)
    private String mimeType;

    /**
     * File size in bytes.
     */
    @Column
    private long fileSize;

    /**
     * Stored file URI.
     */
    @Column(length = 1024)
    private String fileUri;

    /**
     * Stable storage key derived from the URI.
     */
    @Column(length = 256)
    private String uriKey;

    /**
     * Storage backend mode.
     */
    @Column(length = 32)
    private String storeMode;

    /**
     * Original resource name.
     */
    @Column(length = 256)
    private String resourceName;

    /**
     * Storage region.
     */
    @Column(length = 64)
    private String region;

    /**
     * Directory name in the storage backend.
     */
    @Column(length = 256)
    private String directoryName;

    /**
     * Owning module key.
     */
    @Column(length = 128)
    private String moduleKey;

    /**
     * Owning module name.
     */
    @Column(length = 64)
    private String module;
}
