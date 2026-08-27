package com.innospots.nexus.console.role.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.BaseEntity;

/**
 * Binds a USER or ORG_UNIT subject to a role. Effective scope follows the role owner.
 *
 * @see RoleEntity
 */
@Getter
@Setter
@Entity
@Table(name = RoleBindingEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_role_binding_subject",
                columnList = "role_id,subject_type,subject_id", unique = true),
        @Index(name = "idx_nx_role_binding_subject", columnList = "subject_type,subject_id"),
        @Index(name = "idx_nx_role_binding_role", columnList = "role_id")
})
@TableName(RoleBindingEntity.TABLE_NAME)
public class RoleBindingEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_role_binding";

    /**
     * Role binding identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String bindingId;

    @Override
    public String idPrefix() {
        return "rbn";
    }

    /**
     * Bound role identifier.
     */
    @Column(length = 32, nullable = false)
    private String roleId;

    /**
     * Subject type: USER or ORG_UNIT.
     */
    @Column(length = 32, nullable = false)
    private String subjectType;

    /**
     * Subject identifier whose meaning follows {@code subjectType}.
     */
    @Column(length = 32, nullable = false)
    private String subjectId;
}
