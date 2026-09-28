package com.innospots.nexus.console.dictionary.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

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
@Schema(name = "DictionaryTypeCreateRequest", description = "创建字典类型请求")
public record DictionaryTypeCreateRequest(
        @Schema(description = "工作区与安全域内唯一的稳定类型编码", required = true)
        String typeCode,
        @Schema(description = "显示名称", required = true)
        String typeName,
        @Schema(description = "PLATFORM 或 TENANT", required = true)
        SecurityRealm securityRealm,
        @Schema(description = "显示顺序")
        Integer sortOrder
) {
}
