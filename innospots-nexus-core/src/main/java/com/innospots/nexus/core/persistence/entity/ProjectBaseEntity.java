package com.innospots.nexus.core.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * 工作区内项目范围基类实体，在 {@link WorkspaceBaseEntity} 基础上增加项目 ID，
 * 审计填充时从 {@link com.innospots.nexus.base.thread.TLC#projectId()} 自动写入。
 * <p>仅当设计评审明确需要项目级持久化隔离时使用；新业务实体默认仍继承 {@link WorkspaceBaseEntity}。</p>
 */
@Getter
@Setter
@MappedSuperclass
public class ProjectBaseEntity extends WorkspaceBaseEntity {

    /** 记录所属项目，从线程本地上下文自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    @Column(length = 32, nullable = false)
    private String projectId;
}
