package com.innospots.nexus.console.dictionary.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 管理控制台字典类型视图。
 *
 * @author Smars
 * @date 2026/09/13
 * @param dictionaryTypeId type 标识符
 * @param typeCode         稳定的类型编码
 * @param typeName         显示名称
 * @param securityRealm    PLATFORM 或 TENANT
 * @param status           生命周期状态
 * @param sortOrder        显示顺序
 * @param builtIn          类型是否由系统管理
 * @param createdAt        创建时间
 * @param updatedAt        最后更新时间
 */
@Schema(name = "DictionaryTypeVo", description = "字典类型视图")
public record DictionaryTypeVo(
        @Schema(description = "type 标识符", required = true)
        String dictionaryTypeId,
        @Schema(description = "稳定的类型编码", required = true)
        String typeCode,
        @Schema(description = "显示名称", required = true)
        String typeName,
        @Schema(description = "PLATFORM 或 TENANT", required = true)
        SecurityRealm securityRealm,
        @Schema(description = "生命周期状态", required = true)
        BasicStatus status,
        @Schema(description = "显示顺序")
        Integer sortOrder,
        @Schema(description = "类型是否由系统管理", required = true)
        Boolean builtIn,
        @Schema(description = "创建时间", required = true)
        LocalDateTime createdAt,
        @Schema(description = "最后更新时间", required = true)
        LocalDateTime updatedAt
) {
}
