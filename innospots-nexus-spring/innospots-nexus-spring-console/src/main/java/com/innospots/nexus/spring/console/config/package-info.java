/**
 * 管理控制台各业务域 Spring 装配，每个 console 模块对应一个 {@code Console*Configuration}。
 * 配置项通过 {@link ConsoleAuthProperties}、{@link ConsoleCredentialTotpProperties} 等
 * {@code @ConfigurationProperties} 绑定，而非 {@code @Value} 占位符。
 *
 * <p>由 {@link com.innospots.nexus.spring.console.EnableNexusConsole} 统一 {@code @Import}。</p>
 */
package com.innospots.nexus.spring.console.config;
