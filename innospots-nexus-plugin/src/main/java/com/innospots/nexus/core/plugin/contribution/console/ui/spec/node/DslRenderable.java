package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

/**
 * 可渲染 DSL 片段：内联节点或动态源。
 *
 * <p>用于页面 {@code components}、{@code body}、{@code children} 及嵌套节点树。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public sealed interface DslRenderable permits DslNode, DslSourceRef {
}
