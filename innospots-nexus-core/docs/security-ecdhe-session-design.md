# Core 轻量 ECDHE 安全会话设计

## 1. 文档定位

| 项 | 内容 |
|----|------|
| 状态 | 草案（待评审） |
| 日期 | 2026-09-19 |
| 归属模块 | `innospots-nexus-core`（密码学与会话域）；HTTP 暴露归属 `innospots-nexus-service-http` 或各 application 适配层 |
| 关联文档 | 根目录 `AGENTS.md`；历史全量方案（V3.0 等）不在本仓库归档，本稿为相对 V3.0 的**范围收敛** |
| 能力摘要 | `security-core = ECDHE 密钥协商 + 持有证明（Proof of Possession）+ 短期 Security Session` |

---

## 2. 背景与目标

### 2.1 背景

应用需要在 **已有 HTTPS 传输安全** 之上，再建立一层 **短期、双方可独立验证的共享会话密钥**（`authKey`），用于证明客户端与服务端在同一次 ECDHE 握手后持有相同秘密，并可选择性地派生轻量 **Session Credential**，避免仅传 `sessionId` 时等价于 Bearer Token 裸传。

原 V3.0 类方案包含报文加密、请求签名、防重放、密钥轮换、Vault 等；与当前 `innospots-nexus-core`「业务中立、框架无关、依赖最小」定位不符，且实现与运维成本高。

### 2.2 目标

1. 在 Core 内提供 **可复用、无 Spring/Quarkus/Servlet/Jakarta REST 依赖** 的 ECDHE 会话建立与校验能力。
2. 固定算法栈，**不做**可插拔算法注册表。
3. 会话生命周期简单：`CREATED → ACTIVE → EXPIRED`，**无** refresh / rotate。
4. 服务端协商完成后 **不保留** 临时 ECDHE 私钥与 `sharedSecret`，仅保留会话态所需字段。
5. 成功标准：双方可完成两轮握手；客户端可验证 `serverProof`；服务端可验证 `clientProof` 并激活会话；可选地校验 `sessionCredential`；单元测试可覆盖密码学与状态机，无需 Redis/DB。

### 2.3 重要前提

> **ECDHE Core 只允许运行在 HTTPS 之上。** 服务器身份由 TLS 证书链保证；本方案 **不** 替代 TLS，不引入客户端长期签名密钥或服务端应用层证书校验。

---

## 3. 不在本方案中的能力

| 排除项 | 说明 |
|--------|------|
| AES-GCM 报文 / Body 加密 | 传输机密性由 HTTPS 承担 |
| 请求签名、Canonical Request、时间戳防重放 | 不在 Core 范围 |
| Filter / Interceptor / 权限模型 | 归属 service 适配层或 console/kernel |
| JWT 签发与校验 | 上层可绑定 `principalId`，Core 不校验身份合法性 |
| Vault / KMS / 长期客户端密钥 | 仅临时 X25519 密钥对 |
| 密钥刷新、keyVersion、grace period | 过期后 **重新执行完整 ECDHE** |
| 持久化 Session 表 | Core 默认内存 Store；集群 Store 在 adapter 实现 |
| BouncyCastle 依赖 | 使用 JDK `X25519` |
| 可配置 `KeyAgreementAlgorithm` / `CryptoProvider` 体系 | 算法写死 |

---

## 4. 归属与词汇（四步法 ①②）

| 概念 | 英文名 | 归属模块/包 | 技术 ID | 稳定业务键 | 备注 |
|------|--------|------------|---------|-----------|------|
| 临时客户端公钥 | `clientPublicKey` | 协议字段 | Base64URL(X509 SPKI) | — | 单次握手 |
| 临时服务端公钥 | `serverPublicKey` | 协议字段 | 同上 | — | 协商后丢弃服务端私钥 |
| 客户端随机数 | `clientNonce` | 协议字段 | Base64URL(32 raw bytes) | — | HKDF salt 输入 |
| 服务端随机数 | `serverNonce` | 协议字段 | Base64URL(32 raw bytes) | — | HKDF salt 输入 |
| ECDH 共享秘密 | `sharedSecret` | 内存临时 | `byte[]` | — | 不得入 Store |
| 会话鉴权密钥 | `authKey` | `SecuritySession` | 32 bytes | — | HKDF 输出 |
| 握手 transcript 摘要 | `transcriptHash` | `SecuritySession`（CREATED 态） | SHA-256 | — | Proof 绑定 |
| 服务端持有证明 | `serverProof` | 响应字段 | HMAC-SHA256 | — | 客户端校验 |
| 客户端持有证明 | `clientProof` | 请求字段 | HMAC-SHA256 | — | 服务端校验 |
| 会话凭证（可选） | `sessionCredential` | 派生值 | HMAC-SHA256 | — | 见 §6.5 |
| 安全会话 | `SecuritySession` | `core.security.session` | `sessionId` (ULID) | `sessionId` | 非 HTTP Session |
| 主体绑定 | `principalId` | `SecuritySession` | 字符串 | 上层定义 | 可空；Core 不校验 |
| 会话状态 | `SessionStatus` | `core.security.session` | enum | — | CREATED / ACTIVE / EXPIRED |

**AGENTS 归属判定：** 密码学原语、会话编排、内存 Store 接口属于 **core 业务中立基础设施**，符合 `innospots-nexus-core`「可复用平台支持、不绑定 Spring Boot auto-configuration」；**不得**落入 `innospots-nexus-base`（避免膨胀 base 且 session 编排偏中间件能力）。

---

## 5. 边界与包结构（四步法 ③）

### 5.1 包路径（领域优先，单包 ≤ 15 类）

```text
com.innospots.nexus.core.security.session
├── SecuritySessionService          # 对外编排入口
├── SecuritySessionStore            # Port
├── InMemorySecuritySessionStore    # 默认 Adapter
├── SecuritySession                 # 领域记录
├── SessionStatus                   # 枚举
├── SecurityPrincipal               # 可选解析结果
│
com.innospots.nexus.core.security.crypto
├── EcdheKeyAgreement               # Port
├── X25519KeyAgreement              # JDK 实现
├── HkdfSha256                      # HKDF-SHA256
├── SessionProof                    # HMAC proof / credential
├── TranscriptHash                  # transcript 构造
├── SecureRandomBytes               # 32-byte nonce
└── Base64UrlCodec                  # 编解码（或复用 base 已有工具若合适）

com.innospots.nexus.core.security.session.command   # Core 侧入参/出参（非 REST）
├── SessionCreateCommand
├── SessionCreateResult
├── SessionConfirmCommand
└── SessionConfirmResult

com.innospots.nexus.core.security.status
└── SecuritySessionStatusCode       # 模块局部 StatusCode
```

**禁止出现在 core.security：**

- `jakarta.ws.rs`、`spring-web`、Servlet Filter
- Redis / JDBC 实现类（仅接口在 core，实现放 `innospots-nexus-spring-*` / Quarkus adapter 等）
- 与 console/kernel 用户表耦合

### 5.2 与相邻域交互

```text
┌─────────────────┐     HTTPS      ┌──────────────────┐
│ Client          │ ──────────────►│ TLS Termination  │
└────────┬────────┘                └────────┬─────────┘
         │  JSON（transport）                │
         ▼                                   ▼
┌─────────────────────────────────────────────────────┐
│ service-http / spring-console / kernel endpoint      │
│  - 映射 /security/session*                           │
│  - 从 JWT/AppKey 解析 principalId（可选）            │
└────────────────────────┬────────────────────────────┘
                         │ SessionCreateCommand / ...
                         ▼
┌─────────────────────────────────────────────────────┐
│ innospots-nexus-core.security.session                   │
│  SecuritySessionService                              │
└────────────────────────┬────────────────────────────┘
                         │ SecuritySessionStore
                         ▼
              InMemory（默认） / Redis（adapter，未来）
```

### 5.3 Session 作用域

本 `SecuritySession` 为 **应用层密码学会话**，与 base transport `UserSnapshot`、console 登录会话 **不同概念**。命名保持 `SecuritySession` 并在文档与 JavaDoc 中区分，避免与 WebSocket `sessionId` 混用（后者属 service-websocket 域）。

---

## 6. API 与分层契约（四步法 ④）

### 6.1 传输层端点（非 Core 实现）

Core **不**实现 REST；下列为 **推荐 HTTP 契约**，由 `service-http` 或 application 模块委托 `SecuritySessionService`。

| 方法 | 路径 | 请求 | 响应 | 委托 |
|------|------|------|------|------|
| POST | `/security/session` | `SessionCreateRequest` | `SessionCreateResponse` | `create(command, principalId)` |
| POST | `/security/session/confirm` | `SessionConfirmRequest` | `SessionConfirmResponse` | `confirm(command)` |

**SessionCreateRequest（JSON）：**

```json
{
  "clientPublicKey": "Base64Url...",
  "clientNonce": "Base64Url..."
}
```

**SessionCreateResponse：**

```json
{
  "sessionId": "01J...",
  "serverPublicKey": "Base64Url...",
  "serverNonce": "Base64Url...",
  "serverProof": "Base64Url...",
  "expiresAt": 1790000000000
}
```

**SessionConfirmRequest：**

```json
{
  "sessionId": "01J...",
  "clientProof": "Base64Url..."
}
```

**SessionConfirmResponse：**

```json
{
  "sessionId": "01J...",
  "status": "ACTIVE",
  "expiresAt": 1790000000000
}
```

传输 DTO 类名可与上表一致，但 **建议放在 HTTP 模块**；Core 使用 `command` 包内 record，由 adapter 做字段 1:1 映射。

### 6.2 Core 服务接口

```java
public interface SecuritySessionService {

    SessionCreateResult create(SessionCreateCommand command, String principalId);

    SessionConfirmResult confirm(SessionConfirmCommand command);

    SecuritySession requireActiveSession(String sessionId);

    Optional<SecuritySession> findSession(String sessionId);

    void invalidate(String sessionId);

    boolean authenticate(String sessionId, String sessionCredential);
}
```

- `principalId`：**可 null**。调用方在已登录场景传入用户/服务身份 ID；Core 仅存储，不验证。
- `requireActiveSession`：状态非 ACTIVE 或已过期 → 抛 `NexusException`（`SecuritySessionStatusCode`）。
- `authenticate`：校验 §6.5 的 `sessionCredential`；供上层 Filter 可选使用。

### 6.3 分层职责（Core 内）

| 组件 | 职责 |
|------|------|
| `X25519KeyAgreement` | 生成服务端临时密钥对；ECDH；返回 `serverPublicKey` + `sharedSecret`；**调用方不得持久化 sharedSecret** |
| `HkdfSha256` | 从 `sharedSecret`、`clientNonce`、`serverNonce` 派生 `authKey` |
| `TranscriptHash` | 按 §7.1 计算 `transcriptHash` |
| `SessionProof` | `serverProof` / `clientProof` / `sessionCredential` |
| `SecuritySessionService` | 编排 create/confirm/TTL/状态迁移 |
| `SecuritySessionStore` | CRUD + 过期惰性删除（find 时判定） |

**禁止：** Core 内 `endpoint → dao` 式分层；无数据库。

### 6.4 领域记录骨架

```java
public record SecuritySession(
    String sessionId,
    byte[] authKey,
    byte[] transcriptHash,
    String principalId,
    SessionStatus status,
    Instant createdAt,
    Instant expireAt
) {
    // authKey、transcriptHash 不得记录日志；toString 必须脱敏
}

public enum SessionStatus {
    CREATED,
    ACTIVE,
    EXPIRED
}

public record SecurityPrincipal(
    String principalId,
    String sessionId
) {
}
```

**Store 中 CREATED 态** 须保存：`authKey`、`transcriptHash`、双方公钥与 nonce 的副本或足以重算 transcript 的字段（推荐直接存 `transcriptHash`，confirm 时不再重算）。

协商完成后服务端 **不得** 保存 `serverPrivateKey`、`sharedSecret`。

### 6.5 Session Credential（推荐纳入首版 Core API）

仅传 `X-Session-Id` 时，窃取 ID 即可冒充。Core 提供可选校验：

```text
sessionCredential = HMAC-SHA256(authKey, "credential:" + sessionId)
```

编码为 Base64URL。客户端在握手完成后本地计算；后续请求建议同时携带：

```http
X-Session-Id: <sessionId>
X-Session-Credential: <sessionCredential>
```

`SecuritySessionService.authenticate(sessionId, sessionCredential)` 使用 `MessageDigest.isEqual` 比较。**不做** per-request body 签名。

---

## 7. 密码学与协议细节

### 7.1 固定算法

| 用途 | 算法 |
|------|------|
| 密钥协商 | X25519（`KeyPairGenerator` / `KeyAgreement` `"X25519"`） |
| 密钥派生 | HKDF-SHA256，输出 32 字节 |
| 持有证明 | HMAC-SHA256 |
| 随机数 | `SecureRandom`，nonce 长度 32 字节 |
| 编码 | Base64URL（无 padding） |

**协议版本常量：** `CORE_SECURITY_PROTOCOL_VERSION = "core-auth-v1"`（用于 HKDF info 与 transcript）。

### 7.2 HKDF

```text
salt = SHA-256( clientNonce_raw || serverNonce_raw )

authKey = HKDF-SHA256(
    ikm = sharedSecret,
    salt = salt,
    info = UTF-8("core-auth-v1"),
    length = 32
)
```

`clientNonce` / `serverNonce` 在 API 层为 Base64URL 字符串；进入 HKDF 前 **必须先解码为 raw bytes**。

### 7.3 Transcript

防止字段错绑；双方独立计算，必须一致：

```text
transcriptHash = SHA-256(
    UTF-8(version)
    || 0x00
    || UTF-8(sessionId)
    || 0x00
    || raw(clientPublicKey)
    || 0x00
    || raw(serverPublicKey)
    || 0x00
    || clientNonce_raw
    || 0x00
    || serverNonce_raw
)
```

- `version` = `"core-auth-v1"`。
- `raw(*PublicKey)` 为解码后的 X25519 公钥字节（SPKI 或 raw 32-byte，**实现须固定一种**；推荐 Java `X509EncodedKeySpec` 编码后的完整 SPKI bytes，双方一致即可）。
- 分隔符 `0x00` 避免歧义拼接。

### 7.4 Proof

```text
serverProof = HMAC-SHA256(authKey, UTF-8("server-finish") || transcriptHash)
clientProof = HMAC-SHA256(authKey, UTF-8("client-finish") || transcriptHash)
```

比较时使用 `MessageDigest.isEqual`。

### 7.5 握手时序

```text
Client                                      Server
  │ 生成 X25519 临时密钥对 + clientNonce           │
  │────── clientPublicKey + clientNonce ────────>│
  │                              生成服务端密钥对 │
  │                              ECDH → sharedSecret
  │                              HKDF → authKey
  │                              生成 sessionId, serverNonce
  │                              transcriptHash, serverProof
  │                              Store(CREATED)
  │<──── sessionId, serverPublicKey, serverNonce ─│
  │        serverProof, expiresAt                 │
  │ ECDH + HKDF → authKey                        │
  │ 验证 serverProof                             │
  │────── sessionId + clientProof ─────────────>│
  │                              验证 clientProof │
  │                              ACTIVE          │
```

**客户端**在收到 create 响应后本地保存：`authKey`、`sessionId`、`sessionCredential`（可选）、`expiresAt`；**不得**将 `authKey` 发往服务端。

### 7.6 会话 ID

使用 `com.innospots.nexus.base.util.IdGenerator.ulid(...)` 生成，前缀建议 `"sec"` 或空（与项目 ULID 惯例对齐，implement 时二选一并写死）。

---

## 8. 失败与状态码

新建 `com.innospots.nexus.core.security.status.SecuritySessionStatusCode`（实现 `StatusCode`），**不**全部塞进 `NexusStatusCode`。

| 场景 | StatusCode（示意 local） | 抛出边界 | HTTP 映射意图 |
|------|-------------------------|---------|--------------|
| `clientPublicKey` 非法 | `INVALID_CLIENT_PUBLIC_KEY` | `create` | 400 |
| `clientNonce` 长度/格式错误 | `INVALID_NONCE` | `create` | 400 |
| `sessionId` 不存在 | `SESSION_NOT_FOUND` | `confirm` / `requireActive` | 404 |
| 会话已过期 | `SESSION_EXPIRED` | 各读操作 | 401 |
| 状态非 CREATED 时 confirm | `SESSION_INVALID_STATE` | `confirm` | 409 |
| `serverProof` / `clientProof` 不匹配 | `INVALID_PROOF` | 客户端校验 / `confirm` | 401 |
| `sessionCredential` 不匹配 | `INVALID_SESSION_CREDENTIAL` | `authenticate` | 401 |
| Store 并发覆盖（可选） | `SESSION_CONFLICT` | `save` | 409 |

客户端校验 `serverProof` 失败：**不调用 confirm**；本地丢弃临时密钥与 authKey。

---

## 9. 事务、幂等与并发

| 项 | 策略 |
|----|------|
| 事务 | 无 DB；内存 Store 单条 `save` 原子即可 |
| `create` 幂等 | **非幂等**；每次调用产生新 `sessionId` 与新 ECDHE |
| `confirm` 幂等 | 同一 `sessionId` 在 CREATED 态下 **仅允许成功一次**；成功后重复 confirm → `SESSION_INVALID_STATE` |
| 并发 | 同一 `sessionId` 双 confirm：先成功者 ACTIVE，后者失败；`InMemorySecuritySessionStore` 使用 `ConcurrentHashMap` + 状态检查 |
| TTL | `find` / `requireActive` 时若 `Instant.now().isAfter(expireAt)` 则迁移 EXPIRED 并拒绝 |

**TTL 默认值（可配置项放 adapter 的 Java config，Core 构造函数注入）：**

| 状态 | 默认 TTL |
|------|----------|
| CREATED（待 confirm） | 60 秒 |
| ACTIVE | 15 分钟 |

过期后 **不 refresh**；客户端重新 `POST /security/session`。

---

## 10. 兼容与迁移

- **新能力**，无存量数据迁移。
- 若未来引入 Redis Store：保持 `SecuritySessionStore` 接口不变；序列化须加密或限制 Redis ACL（`authKey` 敏感）。
- 与 V3.0 全量方案 **不兼容**；若有外部客户端曾用 encryptedSymKey，需单独版本协商（本稿不覆盖）。

---

## 11. 测试范围

| 类型 | 类名（计划） | 断言要点 |
|------|-------------|---------|
| 密码学 | `X25519KeyAgreementTest` | 双方 ECDH 输出一致 |
| 密码学 | `HkdfSha256Test` | 与 RFC 5869 向量或固定 test vector 一致 |
| 密码学 | `SessionProofTest` | proof / credential 确定性 |
| 密码学 | `TranscriptHashTest` | 字段顺序变化导致 hash 变化 |
| 行为 | `SecuritySessionServiceTest` | 完整握手；错误 proof；过期；重复 confirm |
| Store | `InMemorySecuritySessionStoreTest` | save/find/remove/过期 |
| 契约 | `SecuritySessionStatusCodeTest` | 模块+category+local 唯一 |

**不测：** HTTP 端点（归属 service 契约测试）、Redis Store、权限、JWT。

---

## 12. AGENTS 对齐

| AGENTS 要求 | 本方案 |
|-------------|--------|
| core 业务域中立 | 不耦合 user/role/menu |
| 不绑定 Spring Boot auto-configuration | 仅 Java 构造注入 / 工厂；Spring 配置类放 `innospots-nexus-spring-core` 或 application |
| 禁止 console 管理功能 | 无 CRUD 管理端点 |
| base middleware-free | 密码学在 core，不新增 base 依赖 |
| 新 public API 两消费者 | 预期 consumer：service-http 安全过滤器、kernel/console 登录后增强（implement 时登记） |

**无需修改根 `AGENTS.md` 模块表**；若后续将「安全会话」列为 core 一等公民，可在 `java:project` 或文档 PR 中增补一句模块职责说明。

---

## 13. 不建什么（防过度设计）

- 不建 `CredentialAlgorithmRegistry`、`CurveConfig`、`CryptoStrategy`。
- 不建 `SecurityVault`、`KeyRotationScheduler`。
- 不建 `RequestSignatureValidator`、`ReplayNonceStore`。
- 不建 Maven 子模块 `innospots-nexus-security`（能力落在现有 `innospots-nexus-core` 包内）。
- 不在 core 引入 BouncyCastle、Tink。
- 不为 Session 建 MyBatis 表（除非未来单独 ADR 要求审计持久化）。

---

## 14. 架构约束自检

- [x] 依赖方向：core → base only（复用 `IdGenerator`、`NexusException`、`StatusCode`）
- [x] 无 Jakarta REST / Servlet / Spring 于 core
- [x] ports and adapters：`SecuritySessionStore`、`EcdheKeyAgreement` 可替换实现
- [x] HTTPS 前提已在文档与运行手册中声明
- [x] 敏感字段不日志、不 `toString` 泄露

---

## 15. 已确认决策（来自需求收敛）

1. 范围仅 ECDHE + HKDF + HMAC Proof + Session 生命周期 + 可选 Session Credential。
2. 算法固定 X25519 / HKDF-SHA256 / HMAC-SHA256 / Base64URL。
3. 无报文加密、无请求签名、无拦截器、无权限判断。
4. 服务端不长期保存 ECDHE 私钥；Session 存 `authKey` 等最小集。
5. 默认 InMemory Store；Redis 后续 adapter。
6. Session Credential 纳入 Core API（§6.5）。

---

## 16. 开放问题

| # | 问题 | 建议默认 | 决策截止 |
|---|------|----------|----------|
| 1 | HTTP DTO 首版落在哪个模块？ | `innospots-nexus-service-http` + spring 绑定示例 | implement 前 |
| 2 | X25519 公钥 wire 格式：SPKI vs raw 32-byte | SPKI（`X509EncodedKeySpec`） | implement 前 |
| 3 | `SecuritySession` TTL 配置是否进 `application.yaml` | 是，经 Spring `@ConfigurationProperties`，Core 只接收 `Duration` | adapter PR |
| 4 | confirm 前客户端 proof 失败是否上报审计事件 | 否（首版）；observability 可选计数指标 | 可选 |

---

## 附录 A：Core `command` record 骨架

```java
public record SessionCreateCommand(
    String clientPublicKey,
    String clientNonce
) {
}

public record SessionCreateResult(
    String sessionId,
    String serverPublicKey,
    String serverNonce,
    String serverProof,
    Instant expiresAt
) {
}

public record SessionConfirmCommand(
    String sessionId,
    String clientProof
) {
}

public record SessionConfirmResult(
    String sessionId,
    SessionStatus status,
    Instant expiresAt
) {
}
```

## 附录 B：客户端实现要点（非 Core 代码）

1. 生成临时 X25519 密钥对与 32-byte `clientNonce`。
2. 调用 create；用 `serverPublicKey` 做 ECDH + HKDF（与 §7.2 相同）得到 `authKey`。
3. 本地计算 `transcriptHash` 与 `serverProof`，与响应比对。
4. 计算 `clientProof` 调用 confirm。
5. 保存 `sessionCredential` 供后续请求（若采用 §6.5）。

---

*文档结束。评审通过后状态改为「已接受」，再进入 `java:develop` 实现计划。*
