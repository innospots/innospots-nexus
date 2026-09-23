/**
 * 页面 DSL 文档的核心结构校验。
 *
 * <p>校验 schema 形态、必填字段及文档内引用（如组件名称与数据源键）。不校验组件属性、
 * 已注册动作、服务、表达式或真实权限是否存在——这些属于运行时注册表与 linter。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
package com.innospots.nexus.core.plugin.contribution.console.ui.spec.validation;
