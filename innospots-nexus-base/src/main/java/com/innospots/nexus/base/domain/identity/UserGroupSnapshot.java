package com.innospots.nexus.base.domain.identity;

import com.innospots.nexus.base.domain.enums.BasicStatus;

import java.util.List;

/**
 * 具有层级结构（父组）、负责人与协助人的用户组/团队。
 *
 * @author Smars
 * @date 2026/09/13
 * @param groupId          用户组 ID
 * @param groupName        用户组名称
 * @param groupCode        用户组编码
 * @param parentGroupId    父组 ID
 * @param headUserId       负责人用户 ID
 * @param assistantUserIds 协助人用户 ID 列表
 * @param status           状态
 */
public record UserGroupSnapshot(
        Long groupId,
        String groupName,
        String groupCode,
        Long parentGroupId,
        Long headUserId,
        List<Long> assistantUserIds,
        BasicStatus status
) {

    public UserGroupSnapshot {
        assistantUserIds = assistantUserIds == null ? List.of() : List.copyOf(assistantUserIds);
    }
}
