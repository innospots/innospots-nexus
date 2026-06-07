package com.innospots.nexus.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base entity scoped to a specific project. Extends {@link BaseEntity}
 * with a project ID that is auto-populated from {@link
 * com.innospots.nexus.base.thread.TLC#projectId()} during audit fill.
 */
@Getter
@Setter
@MappedSuperclass
public class ProjectBaseEntity extends BaseEntity {

    /** The project this record belongs to. Auto-filled from thread-local context. */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column
    private Long projectId;
}
