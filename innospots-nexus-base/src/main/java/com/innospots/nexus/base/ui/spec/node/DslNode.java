package com.innospots.nexus.base.ui.spec.node;

/**
 * UI tree node declared by {@code type} or {@code component}.
 *
 * <p>This sealed branch covers inline nodes only. Dynamic {@link DslSourceRef} fragments are
 * modeled separately because they load UI structure from remote sources.</p>
 */
public sealed interface DslNode extends DslRenderable permits ComponentNode, ComponentReferenceNode {
}
