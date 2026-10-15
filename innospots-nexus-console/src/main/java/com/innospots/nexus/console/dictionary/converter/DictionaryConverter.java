package com.innospots.nexus.console.dictionary.converter;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.dictionary.domain.entity.DictionaryItemEntity;
import com.innospots.nexus.console.dictionary.domain.entity.DictionaryTypeEntity;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryItemVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.support.DictionaryTypeNames;

/**
 * 字典实体与 VO 的转换。
 */
public final class DictionaryConverter {

    public static final DictionaryConverter INSTANCE = new DictionaryConverter();

    private DictionaryConverter() {
    }

    public DictionaryTypeVo toTypeVo(DictionaryTypeEntity entity) {
        return new DictionaryTypeVo(
                entity.getDictionaryTypeId(),
                entity.getTypeCode(),
                DictionaryTypeNames.read(entity),
                SecurityRealm.valueOf(entity.getSecurityRealm()),
                BasicStatus.valueOf(entity.getStatus()),
                entity.getSortOrder(),
                entity.getBuiltIn(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public DictionaryTypeOptionVo toTypeOption(DictionaryTypeEntity entity) {
        return new DictionaryTypeOptionVo(
                entity.getDictionaryTypeId(),
                entity.getTypeCode(),
                DictionaryTypeNames.displayLabel(entity));
    }

    public DictionaryItemVo toItemVo(DictionaryItemEntity entity) {
        return new DictionaryItemVo(
                entity.getDictionaryItemId(),
                entity.getTypeCode(),
                entity.getItemValue(),
                entity.getItemName(),
                SecurityRealm.valueOf(entity.getSecurityRealm()),
                BasicStatus.valueOf(entity.getStatus()),
                entity.getSortOrder(),
                entity.getBuiltIn(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public String formatStatus(BasicStatus status) {
        return status == null ? BasicStatus.ENABLED.name() : status.name();
    }
}
