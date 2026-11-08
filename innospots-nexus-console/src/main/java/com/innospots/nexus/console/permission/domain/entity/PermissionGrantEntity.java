package com.innospots.nexus.console.permission.domain.entity;

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

import com.innospots.nexus.console.permission.domain.enums.PermissionGrantKeyType;
import com.innospots.nexus.core.persistence.entity.OwnershipEntity;

/**
 * 角色或组织单元对单个 catalog 授权项的授权记录；归属列表示授权生效的工作区（或租户）上下文。
 */
@Getter
@Setter
@Entity
@Table(name = PermissionGrantEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_permission_grant_subject_grant",
                columnList = "owner_type,owner_id,security_realm,subject_type,subject_id,grant_key", unique = true),
        @Index(name = "idx_nx_permission_grant_subject",
                columnList = "owner_type,owner_id,subject_type,subject_id"),
        @Index(name = "idx_nx_permission_grant_key",
                columnList = "owner_type,owner_id,grant_key"),
        @Index(name = "idx_nx_permission_grant_realm", columnList = "security_realm")
})
@TableName(PermissionGrantEntity.TABLE_NAME)
public class PermissionGrantEntity extends OwnershipEntity {

    public static final String TABLE_NAME = "nx_permission_grant";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String grantId;

    @Column(length = 32, nullable = false)
    private String subjectType;

    @Column(length = 32, nullable = false)
    private String subjectId;

    @Column(name = "grant_key", length = 256, nullable = false)
    private String grantKey;

    /**
     * catalog 授权项层级：{@link PermissionGrantKeyType#MODULE} / {@link PermissionGrantKeyType#MENU} 的 {@link PermissionGrantKeyType#code()}。
     */
    @Column(name = "grant_key_type", length = 16, nullable = false)
    private String grantKeyType;

    @Column(columnDefinition = "text")
    private String constraintDefinition;

    /**
     * 是否由启动配置文件声明。
     *
     * <p>只读授权不允许在管理端撤销；任何变更都必须修改 classpath 下的角色授权
     * 配置文件后重启生效。</p>
     */
    @Column(nullable = false)
    private Boolean readOnly;

    @Override
    public String idPrefix() {
        return "PGR";
    }
}
