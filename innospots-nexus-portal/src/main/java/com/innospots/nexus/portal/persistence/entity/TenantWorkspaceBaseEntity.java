package com.innospots.nexus.portal.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * 租户内工作区范围基类，在 {@link TenantBaseEntity} 上增加工作区 ID。
 */
@Getter
@Setter
@MappedSuperclass
public class TenantWorkspaceBaseEntity extends TenantBaseEntity {

    /** 记录所属工作区，从线程本地上下文自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    @Column(length = 32, nullable = false)
    private String workspaceId;
}
