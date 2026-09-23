package com.innospots.nexus.kernel.scope.domain.request;

/**
 * 在当前租户业务作用域内激活工作区。
 *
 * @param tenantId    用于校验成员关系的租户
 * @param workspaceId 待激活的工作区
 */
public record SelectWorkspaceRequest(String tenantId, String workspaceId) {
}
