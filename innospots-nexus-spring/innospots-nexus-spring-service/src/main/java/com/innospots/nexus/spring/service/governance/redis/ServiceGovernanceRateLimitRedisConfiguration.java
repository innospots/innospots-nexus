package com.innospots.nexus.spring.service.governance.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.governance.RateLimitProvider;
import com.innospots.nexus.service.governance.config.GovernanceConfig;
import com.innospots.nexus.service.governance.redis.ratelimit.LettuceRateLimitRedisClient;
import com.innospots.nexus.service.governance.redis.ratelimit.RateLimitRedisClient;
import com.innospots.nexus.service.governance.redis.ratelimit.RateLimitRedisSettings;
import com.innospots.nexus.service.governance.redis.ratelimit.RedisTokenBucketRateLimitProvider;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

/**
 * 当 {@code service.governance.rate-limit.store=redis} 时注册 Redis {@link RateLimitProvider}。
 */
@Configuration
@ConditionalOnProperty(prefix = "service.governance.rate-limit", name = "store", havingValue = "redis")
@EnableConfigurationProperties(ServiceGovernanceRateLimitRedisProperties.class)
public class ServiceGovernanceRateLimitRedisConfiguration {

    @Bean(destroyMethod = "shutdown")
    RedisClient serviceRateLimitRedisClient(ServiceGovernanceRateLimitRedisProperties properties) {
        Checks.notNull(properties, "properties");
        return RedisClient.create(RedisURI.create(properties.getUri()));
    }

    @Bean(destroyMethod = "close")
    StatefulRedisConnection<String, String> serviceRateLimitRedisConnection(RedisClient serviceRateLimitRedisClient) {
        return serviceRateLimitRedisClient.connect();
    }

    @Bean
    RateLimitRedisSettings serviceRateLimitRedisSettings(ServiceGovernanceRateLimitRedisProperties properties) {
        return new RateLimitRedisSettings(properties.getKeyPrefix(), properties.getBucketTtlSeconds());
    }

    @Bean
    RateLimitRedisClient serviceRateLimitRedisClientFacade(
            StatefulRedisConnection<String, String> serviceRateLimitRedisConnection) {
        return new LettuceRateLimitRedisClient(serviceRateLimitRedisConnection);
    }

    @Bean
    RateLimitProvider serviceRateLimitProvider(
            GovernanceConfig serviceGovernanceConfig,
            RateLimitRedisSettings serviceRateLimitRedisSettings,
            RateLimitRedisClient serviceRateLimitRedisClientFacade) {
        return new RedisTokenBucketRateLimitProvider(
                serviceGovernanceConfig,
                serviceRateLimitRedisSettings,
                serviceRateLimitRedisClientFacade);
    }
}
