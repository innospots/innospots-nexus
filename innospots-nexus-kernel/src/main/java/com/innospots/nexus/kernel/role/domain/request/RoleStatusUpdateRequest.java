package com.innospots.nexus.kernel.role.domain.request;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Request for enabling or disabling a role.
 *
 * @param status target role status
 */
public record RoleStatusUpdateRequest(BasicStatus status) {
}
