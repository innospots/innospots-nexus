package com.innospots.nexus.console.dictionary.domain.request;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Request for enabling or disabling a dictionary item.
 *
 * @param status target item status
 */
public record DictionaryItemStatusUpdateRequest(BasicStatus status) {
}
