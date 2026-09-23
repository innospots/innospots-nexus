package com.innospots.nexus.base.domain.workspace;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 租户工作区（{@code nx_workspace}）的会话/传输快照。
 * 工作区是租户下的资源共享边界。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId      租户 ID
 * @param workspaceId   工作区 ID
 * @param workspaceCode 工作区编码
 * @param workspaceName 工作区名称
 * @param status        状态
 * @see com.innospots.nexus.base.domain.tenant.TenantSnapshot
 */
public record WorkspaceSnapshot(
        String tenantId,
        String workspaceId,
        String workspaceCode,
        String workspaceName,
        BasicStatus status
) {
}
