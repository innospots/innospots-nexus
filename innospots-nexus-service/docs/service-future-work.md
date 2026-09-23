# 待完善能力与明确排除项

本文单独维护**尚未完成、仅部分落地或依赖宿主/环境集成**的工作项。已实现能力见 [../README.md](../README.md)；设计细节仍见各专题文档，但不再用「一期 / 二期 / Mx」描述交付节奏。

---

## 1. 适配层与工程

| 项 | 现状 | 目标 |
|----|------|------|
| Quarkus **构建期**注解与元数据 | `innospots-nexus-quarkus-service-deployment` 已存在；`ServiceProcessor` 当前仅注册扩展特性名 | 按 [适配设计 §4.1](service-adapter-design.md) 落地 Jandex 扫描、`ServiceBoundary` binding、operation descriptor 生成与构建期失败检查 |
| 上传参数绑定 | transfer 中立类型与校验 SPI 已有；Spring 文档说明未提供统一 `@RequestPart` → `UploadResource` 解析器 | 实现 `ServletUploadAdapter` / `ReactiveUploadAdapter`（及 Quarkus 对等物），与 [运行时 §6.1](service-runtime-design.md) 对齐 |
| Quarkus **native** 门禁 | JVM 黑盒场景已通过 `adapter-test` | 将 native 构建与同一套场景纳入正式 CI（扩展交付门禁） |
| 配置热更新 | 设计为重启生效 | 若需要运行时改策略，须引入不可变 revision，且不改变在途许可所属配置 |

## 2. 安全与身份

| 项 | 说明 |
|----|------|
| JWT / OIDC **一等** `SecurityProvider` | 设计支持宿主 Provider；console compact token 桥接已规划。需按环境配置 issuer、audience、JWK 轮换，不在中立库实现密码学 |
| 可选 Spring Security 桥接 | 非默认依赖；若启用须独立条件配置，不得第二条 FilterChain（见 D2） |
| **回放防护** SPI | 与幂等不同：签名时间戳 / nonce；尚未作为中立 SPI 交付 |
| 病毒扫描 / MIME 探测 SDK | `MalwareScanner` 契约已有；具体引擎由宿主或环境集成，版本在 BOM 登记前须兼容检查 |

## 3. WebSocket 与流

| 项 | 说明 |
|----|------|
| WS **动态资源约束** | 消息 type 白名单与 `permissionKeys` 已支持；更丰富的按消息动态资源解析仍可增强 |
| 按 deployment **生成** WS 端点 | 当前推荐「宿主薄端点 + 标准 Handler」；若要做到零薄壳注册，依赖 Quarkus deployment 增强 |
| SSE **持久重放** | Last-Event-ID 不隐式恢复旧会话；业务需显式恢复协议 |
| **NDJSON** 适配器写路径 | 中立 `NdjsonStreamEncoder` 已有；各宿主 result handler 的默认启用策略可按产品统一 |

## 4. 文件与存储

| 项 | 说明 |
|----|------|
| 大对象 **ResourceContentReader** 绑定 | 契约与 `DefaultDownloadPlanner` 已有；宿主须为各 `ResourceStore` 提供真实流读，缺能力时 `SRV150017` |
| 多段 Range、断点续传上传 | 设计有单段 Range 策略；多 Range 与可续传上传不在当前范围 |
| `service.file.enabled` 默认 | 配置默认 `false`；生产启用须白名单 MIME/扩展名与 Reader |

## 5. 治理、幂等与审计

| 项 | 说明 |
|----|------|
| **通用重试引擎** | 明确不做（D15）；仅文档原则：幂等下游可自建重试 |
| **分布式**限流 | Spring 可选 `service.governance.rate-limit.store=redis`（`innospots-nexus-service-governance-redis`）；Quarkus 仍默认本地 |
| **分布式**会话 / 推送 | 仍仅单 JVM；见 core 会话设计 |
| 幂等 | `InMemoryIdempotencyStore` 单 JVM、有限 TTL；不承诺跨重启 exactly-once；SSE/WS/大结果不重放 |
| 审计 **REQUIRED + 真实事务** | 运行时语义与 `TransactionalAuditStorage` 已有；业务表 / outbox 在 kernel/platform，宿主集成测试需真实 DB（中立库不测 JDBC） |
| 性能 **SLA 数字** | [实施文档 §5](service-implementation-design.md) 为测量目标，非已达成 SLA；须在夹具上对照实测后写入 PR |

## 6. 协议与产品形态（明确不做）

以下能力**不在**本框架范围内，除非未来单独立项并改 AGENTS 边界：

- 分布式 Session、跨节点推送、STOMP / SockJS  
- 持久化 SSE 订阅恢复、通用断点续传上传  
- 重建路由 / DI / Web Server / JSON 引擎  
- IAM 用户表、审计查询管理台、业务 REST 管理端点  

## 7. 开放配置项（环境决策）

上线前由运维 / 安全在宿主配置，中立库不提供默认值冒充生产策略：

| 主题 | 未配置时的安全边界 |
|------|-------------------|
| 生产认证来源、issuer、audience、密钥轮换 | 受保护操作应启动失败或拒绝，不得静默匿名 |
| 审计事实落库与事务边界 | 不宣称仅 JVM 队列等于合规审计 |
| CORS / CSRF / 可信代理 CIDR | 遵循 [运行时 §8](service-runtime-design.md) 默认拒绝策略 |

---

维护约定：新增待办请写入上表并链到相关设计章节；项完成后从本文删除并在根 README「已实现」中简述。
