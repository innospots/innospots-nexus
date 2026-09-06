/**
 * UI tree nodes and dynamic DSL fragments.
 *
 * <p>Inline nodes are declared with {@code type} ({@link com.innospots.nexus.base.ui.spec.node.ComponentNode})
 * or {@code component} ({@link com.innospots.nexus.base.ui.spec.node.ComponentReferenceNode}).
 * Dynamic regions use {@link com.innospots.nexus.base.ui.spec.node.DslSourceRef} to load UI
 * structure from service or HTTP sources. Component-specific {@code props} are intentionally
 * open and validated by the component registry, not by this core model.</p>
 */
package com.innospots.nexus.base.ui.spec.node;
