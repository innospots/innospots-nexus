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

import com.innospots.nexus.core.persistence.entity.OwnershipEntity;

/**
 * 将 USER 或 ORG_UNIT 主体绑定到角色；行归属与角色工作区一致。
 *
 * @author Smars
 * @date 2026/09/13
 * @see RoleEntity
 */
@Getter
@Setter
@Entity
@Table(name = RoleBindingEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_role_binding_subject",
                columnList = "owner_type,owner_id,security_realm,role_id,subject_type,subject_id", unique = true),
        @Index(name = "idx_nx_role_binding_subject", columnList = "subject_type,subject_id"),
        @Index(name = "idx_nx_role_binding_role", columnList = "role_id")
})
@TableName(RoleBindingEntity.TABLE_NAME)
public class RoleBindingEntity extends OwnershipEntity {

    public static final String TABLE_NAME = "nx_role_binding";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String bindingId;

    @Override
    public String idPrefix() {
        return "rbn";
    }

    @Column(length = 32, nullable = false)
    private String roleId;

    @Column(length = 32, nullable = false)
    private String subjectType;

    @Column(length = 32, nullable = false)
    private String subjectId;
}
