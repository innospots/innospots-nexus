package com.innospots.nexus.console.dictionary.domain.request;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 创建字典类型的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param typeCode       工作区与安全域内唯一的稳定类型编码
 * @param typeName       显示名称
 * @param securityRealm  PLATFORM 或 TENANT
 * @param sortOrder      显示顺序
 */
public record DictionaryTypeCreateRequest(
        String typeCode,
        String typeName,
        SecurityRealm securityRealm,
        Integer sortOrder
) {
}
