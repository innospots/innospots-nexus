package com.innospots.nexus.console.dictionary.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 启用或禁用字典项的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param status 目标字典项状态
 */
@Schema(name = "DictionaryItemStatusUpdateRequest", description = "更新字典项状态请求")
public record DictionaryItemStatusUpdateRequest(
        @Schema(description = "目标字典项状态", required = true)
        BasicStatus status
) {
}
