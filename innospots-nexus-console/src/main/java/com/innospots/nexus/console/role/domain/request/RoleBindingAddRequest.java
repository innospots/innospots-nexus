package com.innospots.nexus.console.role.domain.request;

import java.util.List;

import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;

/**
 * 向角色添加 USER 或 ORG_UNIT 主体的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param subjectType 主体类型
 * @param subjectIds  subject 标识符s to bind
 */
public record RoleBindingAddRequest(
        RoleBindingSubjectType subjectType,
        List<String> subjectIds
) {

    public RoleBindingAddRequest {
        subjectIds = subjectIds == null ? List.of() : List.copyOf(subjectIds);
    }
}
