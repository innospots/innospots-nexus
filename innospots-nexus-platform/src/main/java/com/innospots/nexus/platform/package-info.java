/**
 * 运营管理平台（platform）业务域：租户生命周期、平台 IAM、自助注册与 console entry 贡献。
 *
 * <p>REST 契约前缀 {@code /api/platform}；匿名自助能力在 {@code /api/platform/public} 下。
 * 本模块依赖 {@code innospots-nexus-console}，不得依赖 portal。</p>
 *
 * <p>按职责分包：{@code tenant}、{@code user}、{@code invite}、{@code access}、
 * {@code registration}、{@code auth}、{@code entry}（PageDsl / 插件 SPI）等。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.PlatformModule
 * @see com.innospots.nexus.platform.config.PlatformConstant
 * @see com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins
 */
package com.innospots.nexus.platform;
