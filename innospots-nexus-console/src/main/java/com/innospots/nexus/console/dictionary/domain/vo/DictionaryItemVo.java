package com.innospots.nexus.console.dictionary.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * Management-console dictionary item view.
 *
 * @param dictionaryItemId item identifier
 * @param typeCode         parent type code
 * @param itemValue        stable item value
 * @param itemName         display name
 * @param securityRealm    PLATFORM or TENANT
 * @param status           lifecycle status
 * @param sortOrder        display order
 * @param builtIn          whether the item is system-managed
 * @param createdAt        creation time
 * @param updatedAt        last update time
 */
public record DictionaryItemVo(
        String dictionaryItemId,
        String typeCode,
        String itemValue,
        String itemName,
        SecurityRealm securityRealm,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
