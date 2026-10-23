package com.innospots.nexus.core.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * 工作区范围基类实体，在 {@link BaseEntity} 基础上增加工作区 ID，
 * 审计填充时从 {@link com.innospots.nexus.base.thread.TLC#workspaceId()} 自动写入。
 */
@Getter
@Setter
@MappedSuperclass
public class WorkspaceBaseEntity extends BaseEntity {

    /** 记录所属工作区，从线程本地上下文自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    @Column(length = 32, nullable = false)
    private String workspaceId;
}
