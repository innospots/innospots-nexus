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

import com.innospots.nexus.core.persistence.entity.OwnershipEntity;

/**
 * 租户级字典项；归属与字典类型一致。
 */
@Getter
@Setter
@Entity
@Table(name = DictionaryItemEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_dictionary_item_value",
                columnList = "owner_type,owner_id,security_realm,type_code,item_value", unique = true),
        @Index(name = "idx_nx_dictionary_item_type", columnList = "owner_type,owner_id,type_code,sort_order"),
        @Index(name = "idx_nx_dictionary_item_realm", columnList = "security_realm")
})
@TableName(DictionaryItemEntity.TABLE_NAME)
public class DictionaryItemEntity extends OwnershipEntity {

    public static final String TABLE_NAME = "nx_dictionary_item";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String dictionaryItemId;

    @Override
    public String idPrefix() {
        return "dci";
    }

    @Column(length = 64, nullable = false)
    private String typeCode;

    @Column(length = 64, nullable = false)
    private String itemValue;

    @Column(length = 128, nullable = false)
    private String itemName;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean builtIn;
}
