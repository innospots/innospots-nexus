/**
 * {@code console@2} 插件贡献：领域模型、YAML 解码、运行时目录与 entry 组装门面。
 *
 * <p><strong>对外编写 entry 插件：</strong>{@link com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode}
 * 静态工厂 + {@link com.innospots.nexus.core.plugin.contribution.console.ConsoleContributions}。
 * <strong>宿主装配：</strong>{@link com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionDecoder}、
 * {@link com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionHandler}、
 * {@link com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionSnapshotter} 为独立 SPI 类型。</p>
 *
 * <p>管理台 REST 与 catalog 持久化位于 {@code innospots-nexus-console}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
package com.innospots.nexus.core.plugin.contribution.console;
