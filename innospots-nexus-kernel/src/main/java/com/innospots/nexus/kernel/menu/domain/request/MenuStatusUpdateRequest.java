package com.innospots.nexus.kernel.menu.domain.request;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Menu lifecycle status update.
 *
 * @param status target lifecycle status
 */
public record MenuStatusUpdateRequest(BasicStatus status) {
}
