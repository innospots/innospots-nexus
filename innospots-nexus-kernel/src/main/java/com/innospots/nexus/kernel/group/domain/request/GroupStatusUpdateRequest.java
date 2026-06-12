package com.innospots.nexus.kernel.group.domain.request;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Group lifecycle status update.
 *
 * @param status target status
 */
public record GroupStatusUpdateRequest(BasicStatus status) {
}
