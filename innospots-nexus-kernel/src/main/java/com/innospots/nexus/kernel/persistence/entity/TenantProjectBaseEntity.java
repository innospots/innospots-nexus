package com.innospots.nexus.kernel.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * 租户工作区内项目范围基类，在 {@link TenantWorkspaceBaseEntity} 上增加项目 ID。
 */
@Getter
@Setter
@MappedSuperclass
public class TenantProjectBaseEntity extends TenantWorkspaceBaseEntity {

    /** 记录所属项目，从线程本地上下文自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    @Column(length = 32, nullable = false)
    private String projectId;
}
