package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ChildrenDeserializer;

import java.util.List;

/**
 * Child renderables declared as an array or a single dynamic source reference.
 *
 * @param items inline child nodes when {@link #isArray()} is {@code true}
 * @param sourceRef dynamic DSL source when the YAML value is a single {@code source} object
 */
@JsonDeserialize(using = ChildrenDeserializer.class)
public record Children(List<DslRenderable> items, DslSourceRef sourceRef) {

    /**
     * Creates children from an inline renderable list.
     *
     * @param items child renderables
     * @return children wrapper
     */
    public static Children ofItems(List<DslRenderable> items) {
        return new Children(items, null);
    }

    /**
     * Creates children from one dynamic source reference.
     *
     * @param sourceRef dynamic source reference
     * @return children wrapper
     */
    public static Children ofSourceRef(DslSourceRef sourceRef) {
        return new Children(List.of(), sourceRef);
    }

    /**
     * Returns whether children are declared as an array.
     *
     * @return true for array children
     */
    public boolean isArray() {
        return sourceRef == null;
    }

    /**
     * Returns the inline child list, which may be empty.
     *
     * @return child items
     */
    public List<DslRenderable> items() {
        return items == null ? List.of() : List.copyOf(items);
    }
}
