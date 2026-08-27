package com.innospots.nexus.console.role.domain.request;

import java.util.List;

import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;

/**
 * Request for adding USER or ORG_UNIT subjects to a role.
 *
 * @param subjectType subject kind
 * @param subjectIds  subject identifiers to bind
 */
public record RoleBindingAddRequest(
        RoleBindingSubjectType subjectType,
        List<String> subjectIds
) {

    public RoleBindingAddRequest {
        subjectIds = subjectIds == null ? List.of() : List.copyOf(subjectIds);
    }
}
