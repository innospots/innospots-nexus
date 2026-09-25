package com.innospots.nexus.quarkus.plugin.config;

import java.util.List;

import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;

/**
 * CDI 可注册的 Contribution 处理器集合（避免 {@code List<PluginContributionHandler<?>>} 作为 Bean 类型）。
 */
public final class PluginContributionHandlers {

    private final List<PluginContributionHandler<?>> handlers;

    /**
     * @param handlers 处理器列表；{@code null} 视为空列表
     */
    public PluginContributionHandlers(List<PluginContributionHandler<?>> handlers) {
        if (handlers == null || handlers.isEmpty()) {
            this.handlers = List.of();
        } else {
            this.handlers = List.copyOf(handlers);
        }
    }

    /**
     * 空集合占位。
     */
    public static PluginContributionHandlers empty() {
        return new PluginContributionHandlers(List.of());
    }

    /**
     * 供 {@link PluginHostStartupTaskProducer} 传入 Core 启动请求。
     */
    public List<PluginContributionHandler<?>> handlers() {
        return handlers;
    }
}
