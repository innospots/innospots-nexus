package com.innospots.nexus.service.contract.context;

import com.innospots.nexus.base.util.Checks;

/**
 * Typed key for {@link ContextAttributes}. Names identify attributes; types reject unsafe casts.
 *
 * @param <T> attribute value type
 * @param name unique attribute name
 * @param type expected runtime class
 * @author Smars
 * @date 2026/09/13
 * @see ContextAttributes
 */
public record AttributeKey<T>(String name, Class<T> type) {

    public AttributeKey {
        Checks.notBlank(name, "name");
        Checks.notNull(type, "type");
    }
}
