# innospots-nexus-service-governance-redis

基于 **Lettuce** 的 `RateLimitProvider` 实现，将令牌桶状态存放在 **Redis**，供多副本 / 多服务共享配额。

## 使用

1. 依赖本模块（Spring 已由 `innospots-nexus-spring-service` 传递引入）。
2. 配置 `service.governance.rate-limit.store=redis` 与 `service.governance.rate-limit.redis.*`。
3. 在 `GovernanceConfig.rateLimits` 中定义策略；按客户限流使用 `RateLimitDimensionMode.CUSTOMER` 并在 principal 上设置 `customerId`。

详见 [rate-limit.md](../innospots-nexus-service-governance/docs/rate-limit.md) §9。

- 墙钟毫秒时间戳；Redis 不可用时失败关闭（`SYSTEM_ERROR`）。
- `maxRateLimitKeys` 仅约束本地 Provider。
