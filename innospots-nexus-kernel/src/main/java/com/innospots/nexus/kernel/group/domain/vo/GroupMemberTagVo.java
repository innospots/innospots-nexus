package com.innospots.nexus.kernel.group.domain.vo;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Capability tag assigned to a group member.
 *
 * @param tagName     tag name used by the assignment
 * @param description optional tag description
 * @param status      tag definition status
 */
public record GroupMemberTagVo(
        String tagName,
        String description,
        BasicStatus status
) {
}
