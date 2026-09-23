package com.innospots.nexus.base.domain.project;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 工作区内项目的会话/传输快照。
 * 项目在共享工作区下提供业务隔离。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId    租户 ID
 * @param workspaceId 工作区 ID
 * @param projectId   项目 ID
 * @param projectCode 项目编码
 * @param projectName 项目名称
 * @param description 项目描述
 * @param status      状态
 * @see com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot
 */
public record ProjectSnapshot(
        String tenantId,
        String workspaceId,
        String projectId,
        String projectCode,
        String projectName,
        String description,
        BasicStatus status
) {
}
