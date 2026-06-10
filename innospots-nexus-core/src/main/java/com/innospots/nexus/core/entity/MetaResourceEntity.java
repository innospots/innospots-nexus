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
 * JPA/MyBatis-Plus entity for stored file/resource metadata.
 * <p>Maps to the {@code nexus_meta_resource} table and inherits audit
 * fields from {@link ProjectBaseEntity}.</p>
 */
@Getter
@Setter
@Entity
@Table(name = MetaResourceEntity.TABLE_NAME)
@TableName(MetaResourceEntity.TABLE_NAME)
public class MetaResourceEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nexus_meta_resource";

    @TableId(type = IdType.INPUT)
    @Id
    @Column(length = 32, nullable = false)
    private String resourceId;
    @Column(length = 128)
    private String mimeType;
    @Column
    private long fileSize;
    @Column(length = 1024)
    private String fileUri;
    @Column(length = 256)
    private String uriKey;
    @Column(length = 32)
    private String storeMode;
    @Column(length = 256)
    private String resourceName;
    @Column(length = 64)
    private String region;
    @Column(length = 256)
    private String dirName;
    @Column(length = 128)
    private String moduleKey;
    @Column(length = 64)
    private String module;
}
