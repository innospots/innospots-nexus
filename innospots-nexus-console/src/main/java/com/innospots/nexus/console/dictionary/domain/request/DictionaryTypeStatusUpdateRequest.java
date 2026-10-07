package com.innospots.nexus.console.dictionary.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 启用或禁用字典类型的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param status 目标类型状态
 */
@Schema(name = "DictionaryTypeStatusUpdateRequest", description = "更新字典类型状态请求")
public record DictionaryTypeStatusUpdateRequest(
        @Schema(description = "目标类型状态", required = true)
        BasicStatus status
) {
}
