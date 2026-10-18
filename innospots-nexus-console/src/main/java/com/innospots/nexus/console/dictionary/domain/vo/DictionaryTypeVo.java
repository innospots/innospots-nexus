package com.innospots.nexus.console.dictionary.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.dictionary.json.DictionaryTypeI18nSerializer;

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
 * @param items            字典项（详情与写入响应时返回；类型分页不填充）
 */
@Schema(name = "DictionaryTypeVo", description = "字典类型视图")
public record DictionaryTypeVo(
        @Schema(description = "type 标识符", required = true)
        String dictionaryTypeId,
        @Schema(description = "稳定的类型编码", required = true)
        String typeCode,
        @Schema(description = "多语言显示名称", required = true)
        @JsonSerialize(using = DictionaryTypeI18nSerializer.class)
        I18nObject typeName,
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
        LocalDateTime updatedAt,
        @Schema(description = "字典项（详情与写入响应时返回）")
        List<DictionaryItemVo> items
) {
}
