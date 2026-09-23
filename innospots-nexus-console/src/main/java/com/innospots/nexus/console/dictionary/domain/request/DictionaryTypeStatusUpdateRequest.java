package com.innospots.nexus.console.dictionary.domain.request;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 启用或禁用字典类型的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param status 目标类型状态
 */
public record DictionaryTypeStatusUpdateRequest(BasicStatus status) {
}
