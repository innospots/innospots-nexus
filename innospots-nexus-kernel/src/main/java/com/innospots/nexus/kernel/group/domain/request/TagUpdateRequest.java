package com.innospots.nexus.kernel.group.domain.request;

/**
 * Mutable project tag data.
 * <p>
 * A tag-name change requires a transactional service to update member tag
 * assignments without using joined DAO statements.
 * </p>
 *
 * @param tagName     target project-unique tag name
 * @param description optional description
 */
public record TagUpdateRequest(String tagName, String description) {
}
