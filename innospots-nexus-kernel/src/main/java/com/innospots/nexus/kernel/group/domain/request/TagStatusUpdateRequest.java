package com.innospots.nexus.kernel.group.domain.request;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Project tag lifecycle status update.
 *
 * @param status target status
 */
public record TagStatusUpdateRequest(BasicStatus status) {
}
