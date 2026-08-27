package com.innospots.nexus.console.dictionary.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * Management-console dictionary type view.
 *
 * @param dictionaryTypeId type identifier
 * @param typeCode         stable type code
 * @param typeName         display name
 * @param securityRealm    PLATFORM or TENANT
 * @param status           lifecycle status
 * @param sortOrder        display order
 * @param builtIn          whether the type is system-managed
 * @param createdAt        creation time
 * @param updatedAt        last update time
 */
public record DictionaryTypeVo(
        String dictionaryTypeId,
        String typeCode,
        String typeName,
        SecurityRealm securityRealm,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
