package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

/**
 * 以 {@code type} 或 {@code component} 声明的 UI 树节点。
 *
 * <p>此密封分支仅覆盖内联节点。动态 {@link DslSourceRef} 片段单独建模，因其从远程源加载
 * UI 结构。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public sealed interface DslNode extends DslRenderable permits ComponentNode, ComponentReferenceNode {
}
