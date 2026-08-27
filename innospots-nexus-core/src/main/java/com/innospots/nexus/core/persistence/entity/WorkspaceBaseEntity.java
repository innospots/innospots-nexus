package com.innospots.nexus.core.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base entity scoped to a workspace. Extends {@link TenantBaseEntity} with a
 * workspace ID auto-populated from
 * {@link com.innospots.nexus.base.thread.TLC#workspaceId()} during audit fill.
 */
@Getter
@Setter
@MappedSuperclass
public class WorkspaceBaseEntity extends TenantBaseEntity {

    /** Workspace this record belongs to. Auto-filled from thread-local context. */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(length = 32)
    private String workspaceId;
}
