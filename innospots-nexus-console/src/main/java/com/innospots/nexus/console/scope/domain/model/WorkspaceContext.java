package com.innospots.nexus.console.scope.domain.model;

import java.util.List;

import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceEntity;
import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceMemberEntity;

/**
 * 工作区上下文：工作区本体、当前用户的成员关系与该成员在工作区内的角色编码。
 *
 * <p>这是内部解析结果，不直接返回前端。</p>
 *
 * @param workspace 工作区
 * @param member    当前用户在该工作区下的成员关系
 * @param roleCodes 该成员在工作区内持有的角色编码；无绑定为空列表
 * @author Smars
 * @date 2026/11/07
 */
public record WorkspaceContext(
        WorkspaceEntity workspace,
        WorkspaceMemberEntity member,
        List<String> roleCodes
) {

    public WorkspaceContext {
        roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
    }

    /**
     * 工作区标识。
     *
     * @return 工作区标识
     */
    public String workspaceId() {
        return workspace == null ? null : workspace.getWorkspaceId();
    }

    /**
     * 工作区所属租户标识。
     *
     * @return 租户标识
     */
    public String tenantId() {
        return workspace == null ? null : workspace.getTenantId();
    }

    /**
     * 工作区成员标识。
     *
     * @return 成员标识
     */
    public String workspaceMemberId() {
        return member == null ? null : member.getWorkspaceMemberId();
    }
}
