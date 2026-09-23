package com.innospots.nexus.quarkus.service.config;

import com.innospots.nexus.service.contract.policy.ResponseProfile;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * {@code service.*} 配置映射。
 */
@ConfigMapping(prefix = "service")
public interface ServiceConfig {

    /**
     * 是否启用服务适配。
     *
     * @return 启用时为 {@code true}
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * HTTP 错误响应 profile。
     *
     * @return profile
     */
    @WithDefault("LEGACY")
    ResponseProfile responseProfile();
}
