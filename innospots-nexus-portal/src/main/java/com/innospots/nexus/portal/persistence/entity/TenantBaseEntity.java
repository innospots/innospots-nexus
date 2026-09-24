package com.innospots.nexus.portal.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.innospots.nexus.core.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * 租户范围基类实体，在 {@link BaseEntity} 基础上增加租户 ID，
 * 审计填充时从 {@link com.innospots.nexus.base.thread.TLC#tenantId()} 自动写入。
 */
@Getter
@Setter
@MappedSuperclass
public class TenantBaseEntity extends BaseEntity {

    /** 记录所属租户，从线程本地上下文自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    @Column(length = 32, nullable = false)
    private String tenantId;
}
