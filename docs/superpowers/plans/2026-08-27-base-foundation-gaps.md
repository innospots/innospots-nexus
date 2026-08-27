# Base 基础类补齐 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 分三阶段补齐 `innospots-nexus-base` 日常开发助手，去掉上层重复样板，对齐 ID/分页类型。

**Architecture:** 只加 middleware-free 的小工具和现有类接线。不扩 UserInfo/字典等业务模型。不把 JPA 实体、密码策略、对象存储实现放进 base。

**Tech Stack:** Java 25, Jackson 2.22, JUnit 5, 现有 `NexusException` / `NexusStatusCode` / `R` / `PageResult` / `TLC`

**Spec:** 对照当前 base 源码与 kernel/console/platform 调用点，不复制旧 Innospots。

## Global Constraints

- `innospots-nexus-base` 保持 middleware-free（无 Spring / Servlet / DB）
- 不含业务域逻辑
- 代码修改后必须 `mvn clean compile`
- 不新增 Hutool/Commons 门面
- 不改模块 SKILL.md（除非明确要求扫描文档）

---

## 阶段 1：补助手（只加 API，不改现有方法签名）

目标：上层不再手写空值守卫、分页默认值、`R.fail(code, message)` 拼接。

| 动作 | 文件 | 改动说明 |
|---|---|---|
| 新增 | `base/.../util/Checks.java` | `notNull` / `notBlank` / `isTrue`，失败抛 `NexusException.build(INVALID_PARAMETER)` |
| 新增 | `base/.../util/ChecksTest.java` | 覆盖 null、blank、false 三条路径 |
| 修改 | `base/.../util/StringUtils.java` | 补 `isNotBlank` / `isNotEmpty`，与现有 `isBlank` 对称 |
| 修改 | `base/.../util/StringUtilsTest.java` | 补对应断言 |
| 新增 | `base/.../domain/request/Pagination.java` | `DEFAULT_PAGE_NO=1`、`DEFAULT_PAGE_SIZE=20`、`normalizePageNo` / `normalizePageSize` |
| 修改 | `base/.../domain/request/SimpleQueryRequest.java` | compact constructor 改为调用 `Pagination`，去掉重复常量 |
| 修改 | `base/.../domain/response/R.java` | 加 `fail(StatusCode)`、`fail(StatusCode, T data)`、`from(NexusException)` |
| 修改 | `base/.../exception/NexusException.java` | 加 `build(StatusCode, String)`、`build(StatusCode, Throwable)` |
| 修改 | 现有 R / NexusException 测试 | 覆盖新工厂方法 |
| 新增依赖 | `innospots-nexus-base/pom.xml` | 加 `jackson-datatype-jsr310`（版本走 jackson-bom） |
| 修改 | `base/.../json/Jsons.java` | `findAndAddModules()` 或注册 `JavaTimeModule`；加 `fromJson(String, TypeReference)`；读写失败改抛 `SERIALIZATION_FAILED` |
| 修改 | `base/.../json` 测试 | Instant/LocalDateTime 往返；TypeReference 反序列化 |
| 修改 | `base/.../util/CryptoUtils.java` | 加密失败改走 `NexusStatusCode` 的 CRYPTO/SYSTEM 码，不再用 `AES_GCM_*` 字符串 |

本阶段不做：不改 TLC 签名、不删 DataPage、不改上层 `*PageRequest`。

验证：`mvn -pl innospots-nexus-base test`

- [x] 按阶段 1 表逐项实现并补测
- [x] `mvn -pl innospots-nexus-base test`

---

## 阶段 2：对齐类型（有调用方改动）

目标：ID 与分页和现网一致，去掉重复分页容器。

| 动作 | 文件 | 改动说明 |
|---|---|---|
| 修改 | `base/.../thread/TLC.java` | `userId` 从 `Long` 改为 `String`；补 `traceId()` / `sessionId()` getter/setter |
| 修改 | `base/.../thread/TLCTest.java` | 按 String userId 改断言 |
| 修改 | `core/.../handler/AuditMetaObjectHandler.java` | 去掉 `String.valueOf(TLC.userId())` 兜底，直接用 String |
| 修改 | `core/.../CoreEntityContractsTest.java` | `TLC.userId(1001L)` → 字符串 ID |
| 修改 | `base/.../domain/response/PageResult.java` | 加 `from(DataPage)`；分页计算抽到 `Pagination` |
| 修改 | `base/.../domain/data/DataPage.java` | 标 `@Deprecated`，内部改调 `Pagination`；新代码只用 `PageResult` |
| 修改 | `kernel/.../user/operator/UserOperator.java`（及返回 `DataPage` 的调用点） | 返回值改为 `PageResult` |
| 修改 | 5 个 `*PageRequest` | compact constructor 改调 `Pagination.normalize*`，删除本地 `if (pageNo < 1)` |
| | `console/.../DictionaryItemPageRequest.java` | 同上 |
| | `console/.../DictionaryTypePageRequest.java` | 同上 |
| | `console/.../RolePageRequest.java` | 同上 |
| | `console/.../RoleBindingPageRequest.java` | 同上 |
| | `kernel/.../UserPageRequest.java` | 同上 |
| 修改 | `base/.../events/DomainEvent.java` | 去掉每次 new 的 default；改为要求实现提供稳定 `eventId` / `occurredAt`，或提供带缓存的抽象 |
| 修改 | `ResourceEvent`、`TenantCreatedEvent`、`ConversationCreatedEvent`、`SessionMessageCreatedEvent`、相关测试 | 实现稳定 eventId/occurredAt（record 补字段，或构造时生成一次） |

本阶段不做：不改 `UserInfo`（上层未使用，冻结）；不加 Tree。

验证：`mvn test`

---

## 阶段 3：树组装 + 收尾

目标：菜单/组织/权限不再手写 parentId 树；修明显文档错误。

| 动作 | 文件 | 改动说明 |
|---|---|---|
| 新增 | `base/.../util/Tree.java` | `of(list, idFn, parentIdFn, childrenSetter)`，根节点 parentId 为 null/blank |
| 新增 | `base/.../util/TreeTest.java` | 两层树、孤儿节点、空列表 |
| 修改 | `base/.../status/NexusStatusCode.java` | javadoc：fullCode 是 9 位（`NEX`+2+4），不是 6/7 位 |
| 可选 | console 菜单 VO 组装、权限 catalog | 改用 `Tree.of`，仅在有现成列表→树逻辑时替换，不借机重构 |

本阶段明确不做：

- 不扩 `UserInfo` / `OrganizationInfo` / `ProjectInfo` / `DictionaryType`
- 不补 Hutool/Commons 门面
- 不把 `BaseEntity`、密码策略、S3/OSS 实现放进 base
- 不扩 `HttpUtils`、`domain.condition`、`execution.*`
- 不同步 SKILL.md

验证：`mvn test`

---

### Task 1: 阶段 1 助手类

**Files:** 见阶段 1 表

- [x] 按阶段 1 表逐项实现并补测
- [x] `mvn -pl innospots-nexus-base test`
- [ ] 提交（仅当用户要求）

### Task 2: 阶段 2 类型对齐

**Files:** 见阶段 2 表

- [ ] 按阶段 2 表逐项实现并改调用方
- [ ] `mvn test`
- [ ] 提交（仅当用户要求）

### Task 3: 阶段 3 Tree 与收尾

**Files:** 见阶段 3 表

- [ ] 按阶段 3 表实现
- [ ] `mvn test`
- [ ] 提交（仅当用户要求）
