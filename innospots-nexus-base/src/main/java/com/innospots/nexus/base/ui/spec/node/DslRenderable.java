package com.innospots.nexus.base.ui.spec.node;

/**
 * Renderable DSL fragment: inline node or dynamic source.
 *
 * <p>Used for page {@code components}, {@code body}, {@code children}, and nested node trees.</p>
 */
public sealed interface DslRenderable permits DslNode, DslSourceRef {
}
