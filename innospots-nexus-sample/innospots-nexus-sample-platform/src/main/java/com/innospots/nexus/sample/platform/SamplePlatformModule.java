package com.innospots.nexus.sample.platform;

/**
 * 示例运营平台扩展模块标记。
 * <p>
 * 本库在 {@code innospots-nexus-platform} 之上演示按交付面划分的 DDD 包树：
 * {@code console}（管理台）、{@code core}（领域与持久化）、{@code inbound}（对外 API）。
 * 与 Spring / Quarkus 运行时无关，由 sample 应用模块装配。
 * </p>
 *
 * @author Smars
 * @date 2026/09/26
 */
public final class SamplePlatformModule {

    private SamplePlatformModule() {
    }
}
