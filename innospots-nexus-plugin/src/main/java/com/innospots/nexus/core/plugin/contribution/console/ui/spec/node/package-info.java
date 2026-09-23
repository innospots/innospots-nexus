/**
 * UI 树节点与动态 DSL 片段。
 *
 * <p>内联节点以 {@code type}
 * （{@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode}）
 * 或 {@code component}
 * （{@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentReferenceNode}）
 * 声明。动态区域使用
 * {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslSourceRef}
 * 从服务或 HTTP 源加载 UI 结构。组件特定的 {@code props} 有意保持开放，由组件注册表校验，
 * 而非本核心模型。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;
