package com.innospots.nexus.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base JPA/MyBatis-Plus entity with automatic audit fields.
 * <p>All fields are auto-populated by {@link AuditMetaObjectHandler} via
 * MyBatis-Plus meta-object handling — no manual assignment required.</p>
 *
 * @see AuditMetaObjectHandler
 * @see ProjectBaseEntity
 */
@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    /**
     * Returns the prefix used when generating this entity's primary key.
     *
     * @return primary-key prefix, or an empty string when no prefix is required
     */
    public String idPrefix() {
        return "";
    }

    /** Record creation timestamp, set once on insert and never updated. */
    @TableField(fill = FieldFill.INSERT)
    @Column(updatable = false)
    private LocalDateTime createdTime;

    /** Record last-update timestamp, refreshed on every insert and update. */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column
    private LocalDateTime updatedTime;

    /** Identity of the user who created the record. Immutable after insert. */
    @TableField(fill = FieldFill.INSERT)
    @Column(length = 64, updatable = false)
    private String createdBy;

    /** Identity of the user who last updated the record. */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(length = 64)
    private String updatedBy;

}
