package com.innospots.nexus.base.domain.field;

/**
 * A selectable option with a stored value and a display label.
 */
public record SelectOption(
        String value,
        String label
) {
}
