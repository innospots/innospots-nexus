package com.innospots.nexus.console.auth.domain.request;

/**
 * 在当前工作区作用域内激活项目。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId    owning tenant 标识符
 * @param workspaceId owning workspace 标识符
 * @param projectId   待激活的项目
 */
public record SelectProjectRequest(String tenantId, String workspaceId, String projectId) {
}
