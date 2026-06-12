package com.innospots.nexus.kernel.group.domain.entity;

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

import com.innospots.nexus.core.entity.ProjectBaseEntity;

/**
 * Lightweight project-level capability tag definition.
 */
@Getter
@Setter
@Entity
@Table(name = TagEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_tag_project_name", columnList = "project_id,tag_name", unique = true),
        @Index(name = "idx_nx_tag_project_status", columnList = "project_id,status")
})
@TableName(TagEntity.TABLE_NAME)
public class TagEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nx_tag";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String tagId;

    @Override
    public String idPrefix() {
        return "tag";
    }

    @Column(length = 64, nullable = false)
    private String tagName;

    @Column(length = 256)
    private String description;

    @Column(length = 32, nullable = false)
    private String status;
}
