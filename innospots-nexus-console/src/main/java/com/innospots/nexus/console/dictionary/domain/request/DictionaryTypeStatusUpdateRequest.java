package com.innospots.nexus.console.dictionary.domain.request;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Request for enabling or disabling a dictionary type.
 *
 * @param status target type status
 */
public record DictionaryTypeStatusUpdateRequest(BasicStatus status) {
}
