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
 * Realm-scoped dictionary entry under a type code.
 *
 * @see DictionaryTypeEntity
 * @see com.innospots.nexus.base.domain.dictionary.DictionaryItem
 */
@Getter
@Setter
@Entity
@Table(name = DictionaryItemEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_dictionary_item_value",
                columnList = "workspace_id,security_realm,type_code,item_value", unique = true),
        @Index(name = "idx_nx_dictionary_item_type", columnList = "workspace_id,type_code,sort_order"),
        @Index(name = "idx_nx_dictionary_item_realm", columnList = "security_realm")
})
@TableName(DictionaryItemEntity.TABLE_NAME)
public class DictionaryItemEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_dictionary_item";

    /**
     * Dictionary item identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String dictionaryItemId;

    @Override
    public String idPrefix() {
        return "dci";
    }

    /**
     * Parent dictionary type code.
     */
    @Column(length = 64, nullable = false)
    private String typeCode;

    /**
     * Stable item value unique within the type.
     */
    @Column(length = 64, nullable = false)
    private String itemValue;

    /**
     * Display name.
     */
    @Column(length = 128, nullable = false)
    private String itemName;

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
     * Whether the item is system-managed.
     */
    @Column(nullable = false)
    private Boolean builtIn;
}
