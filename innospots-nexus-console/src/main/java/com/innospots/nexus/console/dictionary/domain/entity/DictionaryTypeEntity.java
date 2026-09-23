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
 * 租户级字典类型；默认 {@code ownerType=TENANT}，{@code ownerId=tenantId}。
 */
@Getter
@Setter
@Entity
@Table(name = DictionaryTypeEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_dictionary_type_code",
                columnList = "owner_type,owner_id,security_realm,type_code", unique = true),
        @Index(name = "idx_nx_dictionary_type_status", columnList = "owner_type,owner_id,status"),
        @Index(name = "idx_nx_dictionary_type_realm", columnList = "security_realm")
})
@TableName(DictionaryTypeEntity.TABLE_NAME)
public class DictionaryTypeEntity extends OwnershipEntity {

    public static final String TABLE_NAME = "nx_dictionary_type";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String dictionaryTypeId;

    @Override
    public String idPrefix() {
        return "dct";
    }

    @Column(length = 64, nullable = false)
    private String typeCode;

    @Column(length = 128, nullable = false)
    private String typeName;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean builtIn;
}
