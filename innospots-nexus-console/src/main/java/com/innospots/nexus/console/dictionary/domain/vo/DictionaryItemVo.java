package com.innospots.nexus.console.dictionary.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 管理控制台字典项视图。
 *
 * @author Smars
 * @date 2026/09/13
 * @param dictionaryItemId item 标识符
 * @param typeCode         父类型编码
 * @param itemValue        稳定的字典项值
 * @param itemName         显示名称
 * @param securityRealm    PLATFORM 或 TENANT
 * @param status           生命周期状态
 * @param sortOrder        显示顺序
 * @param builtIn          字典项是否由系统管理
 * @param createdAt        创建时间
 * @param updatedAt        最后更新时间
 */
@Schema(name = "DictionaryItemVo", description = "字典项视图")
public record DictionaryItemVo(
        @Schema(description = "item 标识符", required = true)
        String dictionaryItemId,
        @Schema(description = "父类型编码", required = true)
        String typeCode,
        @Schema(description = "稳定的字典项值", required = true)
        String itemValue,
        @Schema(description = "显示名称", required = true)
        String itemName,
        @Schema(description = "PLATFORM 或 TENANT", required = true)
        SecurityRealm securityRealm,
        @Schema(description = "生命周期状态", required = true)
        BasicStatus status,
        @Schema(description = "显示顺序")
        Integer sortOrder,
        @Schema(description = "字典项是否由系统管理", required = true)
        Boolean builtIn,
        @Schema(description = "创建时间", required = true)
        LocalDateTime createdAt,
        @Schema(description = "最后更新时间", required = true)
        LocalDateTime updatedAt
) {
}
