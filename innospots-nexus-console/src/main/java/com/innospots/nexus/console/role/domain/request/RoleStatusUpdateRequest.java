package com.innospots.nexus.console.role.domain.request;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 启用或禁用角色的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param status 目标角色状态
 */
public record RoleStatusUpdateRequest(BasicStatus status) {
}
