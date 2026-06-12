package com.innospots.nexus.kernel.group.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Project-level capability tag details.
 *
 * @param tagId       tag identifier
 * @param tagName     project-unique tag name
 * @param description optional description
 * @param status      lifecycle status
 * @param memberCount number of member assignments
 * @param createdTime creation time
 * @param updatedTime last update time
 */
public record TagVo(
        String tagId,
        String tagName,
        String description,
        BasicStatus status,
        long memberCount,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
