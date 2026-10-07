package com.innospots.nexus.spring.service.governance.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 分布式限流配置（{@code service.governance.rate-limit.redis.*}）。
 */
@ConfigurationProperties(prefix = "service.governance.rate-limit.redis")
public class ServiceGovernanceRateLimitRedisProperties {

    /** Redis 连接 URI，如 {@code redis://localhost:6379/0}。 */
    private String uri = "redis://127.0.0.1:6379/0";

    /** 桶键前缀。 */
    private String keyPrefix = "nexus:ratelimit:";

    /** 桶 Hash 过期秒数。 */
    private long bucketTtlSeconds = 86_400L;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public long getBucketTtlSeconds() {
        return bucketTtlSeconds;
    }

    public void setBucketTtlSeconds(long bucketTtlSeconds) {
        this.bucketTtlSeconds = bucketTtlSeconds;
    }
}
