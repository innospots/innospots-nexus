/**
 * Console 语言包读取、协议组装与公共 REST 资源。
 *
 * <p>资源遵循 Spring MessageResource 的 properties 结构，由承载 Console 的
 * application 模块通过 classpath 提供；{@code i18n/console/namespaces} 索引声明的
 * 字符串 namespace 对应 {@code i18n/console/<namespace>/messages*.properties}。</p>
 *
 * @author Smars
 * @date 2026/10/26
 * @see com.innospots.nexus.console.i18n.endpoint.I18nBundleEndpoint
 */
package com.innospots.nexus.console.i18n;
