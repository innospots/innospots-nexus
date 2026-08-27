package com.innospots.nexus.console.dictionary.domain.request;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * Request for creating a dictionary type.
 *
 * @param typeCode       stable type code unique within workspace and realm
 * @param typeName       display name
 * @param securityRealm  PLATFORM or TENANT
 * @param sortOrder      display order
 */
public record DictionaryTypeCreateRequest(
        String typeCode,
        String typeName,
        SecurityRealm securityRealm,
        Integer sortOrder
) {
}
