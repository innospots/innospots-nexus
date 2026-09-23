/**
 * Pactor Page DSL 1.0 核心模型与基础设施。
 *
 * <p>本包定义管理控制台渲染器消费的、可序列化的页面 DSL 契约。仅校验结构形态；组件属性、
 * 动作语义、表达式语法与服务绑定由运行时注册表与 linter 校验。</p>
 *
 * <p>页面 YAML 推荐资源布局：</p>
 * <pre>
 * ui-pages/{moduleKey}/{pageId}.yaml
 * </pre>
 *
 * <p>结构权威来源：{@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl}
 * 与 {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.validation.PageDslValidator}。</p>
 *
 * @see com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl
 * @author Smars
 * @date 2026/09/13
 */
package com.innospots.nexus.core.plugin.contribution.console.ui.spec;
