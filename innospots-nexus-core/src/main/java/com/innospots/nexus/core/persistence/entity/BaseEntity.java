package com.innospots.nexus.core.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.innospots.nexus.core.persistence.handler.AuditMetaObjectHandler;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 带自动审计字段的 JPA/MyBatis-Plus 基类实体。
 * <p>所有字段由 {@link AuditMetaObjectHandler} 通过 MyBatis-Plus 元对象填充自动写入，
 * 无需在仓储层手动赋值。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuditMetaObjectHandler
 */
@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    /**
     * 返回生成本实体主键时使用的前缀。
     *
     * @return 主键前缀；无需前缀时返回空字符串
     */
    public String idPrefix() {
        return "";
    }

    /** 记录创建时间，插入时写入且不可更新。 */
    @TableField(fill = FieldFill.INSERT)
    @Column(updatable = false)
    protected LocalDateTime createdAt;

    /** 记录最后更新时间，每次插入与更新时刷新。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column
    protected LocalDateTime updatedAt;

    /** 创建记录的用户标识，插入后不可变。 */
    @TableField(fill = FieldFill.INSERT)
    @Column(length = 64, updatable = false)
    protected String createdBy;

    /** 最后更新记录的用户标识。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(length = 64)
    protected String updatedBy;

}
