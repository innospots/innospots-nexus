package com.innospots.nexus.console.role.domain.request;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;

/**
 * 向角色添加 USER 或 ORG_UNIT 主体的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param subjectType 主体类型
 * @param subjectIds  subject 标识符s to bind
 */
@Schema(name = "RoleBindingAddRequest", description = "添加角色绑定请求")
public record RoleBindingAddRequest(
        @Schema(description = "主体类型", required = true)
        RoleBindingSubjectType subjectType,
        @Schema(description = "待绑定的 subject 标识符列表", required = true)
        List<String> subjectIds
) {

    public RoleBindingAddRequest {
        subjectIds = subjectIds == null ? List.of() : List.copyOf(subjectIds);
    }
}
