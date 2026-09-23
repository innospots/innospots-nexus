/**
 * Pactor DSL 联合类型的 Jackson 适配器。
 *
 * <p>若干 schema 字段接受多种 YAML 形态（例如 {@code actions} 为单个对象或数组，
 * 或 {@code children} 为数组或动态源引用）。这些反序列化器在不削弱 Java 领域模型的前提下
 * 实现该多态性。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;
