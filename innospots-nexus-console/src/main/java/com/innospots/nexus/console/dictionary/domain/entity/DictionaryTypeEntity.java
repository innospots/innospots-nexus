package com.innospots.nexus.console.dictionary.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;

/**
 * Realm-scoped dictionary type catalog. Items reference this record by {@code typeCode}.
 *
 * @see DictionaryItemEntity
 * @see com.innospots.nexus.base.domain.dictionary.DictionaryType
 */
@Getter
@Setter
@Entity
@Table(name = DictionaryTypeEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_dictionary_type_code",
                columnList = "workspace_id,security_realm,type_code", unique = true),
        @Index(name = "idx_nx_dictionary_type_status", columnList = "workspace_id,status"),
        @Index(name = "idx_nx_dictionary_type_realm", columnList = "security_realm")
})
@TableName(DictionaryTypeEntity.TABLE_NAME)
public class DictionaryTypeEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_dictionary_type";

    /**
     * Dictionary type identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String dictionaryTypeId;

    @Override
    public String idPrefix() {
        return "dct";
    }

    /**
     * Stable type code unique within workspace and security realm.
     */
    @Column(length = 64, nullable = false)
    private String typeCode;

    /**
     * Display name.
     */
    @Column(length = 128, nullable = false)
    private String typeName;

    /**
     * Security realm: PLATFORM or TENANT.
     */
    @Column(length = 32, nullable = false)
    private String securityRealm;

    /**
     * Lifecycle status.
     */
    @Column(length = 32, nullable = false)
    private String status;

    /**
     * Sibling display order.
     */
    @Column(nullable = false)
    private Integer sortOrder;

    /**
     * Whether the type is system-managed.
     */
    @Column(nullable = false)
    private Boolean builtIn;
}
