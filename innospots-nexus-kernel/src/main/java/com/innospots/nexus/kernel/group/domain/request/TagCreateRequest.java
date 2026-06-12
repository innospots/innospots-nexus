package com.innospots.nexus.kernel.group.domain.request;

/**
 * Project tag creation data.
 *
 * @param tagName     project-unique tag name
 * @param description optional description
 */
public record TagCreateRequest(String tagName, String description) {
}
