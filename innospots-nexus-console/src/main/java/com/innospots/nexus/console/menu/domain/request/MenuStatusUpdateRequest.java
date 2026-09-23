package com.innospots.nexus.console.menu.domain.request;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 菜单生命周期状态更新。
 *
 * @author Smars
 * @date 2026/09/13
 * @param status target 生命周期状态
 */
public record MenuStatusUpdateRequest(BasicStatus status) {
}
