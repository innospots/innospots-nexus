---
name: java:design
display_name: Java 架构与设计
description: |
  Java 架构、模块、接口、类与技术方案设计。当用户要做领域建模、划分模块与包
  结构、设计接口/类/方法签名、定义 REST 契约、设计领域事件、规划状态机与生命
  周期、做技术选型，或需要产出架构/技术方案评审意见时使用。产出是设计决策与
  契约，不是实现代码。
  触发词：架构设计、方案设计、领域建模、模块划分、接口设计、类设计、API 设计、
  技术选型、事件设计、状态机、契约设计、设计评审。
category: java
version: 1.0.0
---

# 架构、模块、接口与类设计

## 定位

负责**设计决策**：这块能力归谁、边界在哪、契约长什么样、状态怎么流转。
产出是设计结论与契约骨架，**不是**可运行实现。实现交给 `java:develop`。

## 设计四步法

```text
1. 定归属  →  哪个 Maven 模块、哪个业务域拥有这个能力
2. 建词汇  →  统一术语，区分技术 ID 与稳定业务键
3. 划边界  →  领域数据形状、分层职责、对外契约面
4. 定契约  →  方法签名、不可变性、空值、异常、事务、兼容面
```

每一步都有出口门禁，未通过不得进入下一步。

## 第一步：定归属

| 判定 | 归属 |
|------|------|
| 业务中立、依赖轻量的通用能力（异常、状态码、JSON、ID、加解密、HTTP） | `base` |
| 业务中立的中间件/数据库/平台基础设施 | `core` |
| 管理台契约与扩展声明 | `console` |
| 认证、用户、角色、权限、菜单、字典、审计 | `kernel` |
| 租户、企业主体、平台用户、平台审计 | `platform` |
| 业务专属基础设施 | 所属业务模块，或独立 adapter / plugin / extension / application 模块 |

`kernel` 与 `platform` 平行且互不依赖。两者需要协作时，走可同时依赖两者的
application/adapter 模块，或把业务中立契约下沉到 `console` / `core`；
**不得**把具体业务事件搬进 core 只为绕过依赖规则。

## 第二步：建词汇

在命名任何类型之前先写下：

- 主业务概念与关联概念
- 归属范围：platform / tenant / workspace / realm
- 技术 ID 与稳定业务键的区别（如 `roleId` 与 `roleCode`）
- `state` / `status` / `mode` / `type` 在本域的确切含义
- 创建、更新、查询、排序、成员、运行时视图各自的名字
- 属于相邻域、不得借作本地同义词的概念
- 每个失败是平台级、领域级还是技术级，以及状态码归属方

同一个概念在端点、实体、DAO、数据库、测试中必须是**同一个词**。
术语冲突必须在建源文件之前解决。

## 第三步：划边界

### 分层与依赖方向

```text
endpoint → service → operator → dao
```

允许简化：`endpoint → operator → dao`、`service → dao`。

- operator：面向单一 DAO 的直接数据操作；可跨多 DAO 但必须简单内聚
- service：非平凡工作流、跨 operator 协调、跨领域、事务、授权感知组装
- operator **不得**依赖 service 或另一个 operator；需要协调多个 operator 的逻辑放 service
- endpoint **不得**直接编排 DAO

### 领域数据形状

| 包 | 类型形态 | 命名 | 用途 |
|----|---------|------|------|
| `domain.entity` | class（非 record） | `*Entity` | 数据库持久化实体 |
| `domain.request` | **record** | `*Request` | 端点输入 |
| `domain.vo` | **record** | `*Vo` | 端点输出 |
| `domain.model` | class / record | 业务概念，无强制后缀 | 既非持久化实体也非传输 record 的内部模型 |
| `domain.enums` | enum | 业务概念单数 | 业务枚举与领域状态码 |
| `domain.event` | **record** | 过去时 + `Event` | 领域事件 |

配置绑定对象与系统配置类型放模块级 `config` 包，**不放 `domain`**。

### 是否引入接口

只在满足以下之一时引入：

1. 公共模块、插件、适配器或 SPI 边界
2. 需要或有意支持多种实现
3. 调用方必须与运行时特定实现隔离
4. 契约本身拥有实现必须遵守的生命周期或资源边界

**不得仅为便于 mock 而给每个具体类配接口。** 只有一个稳定内部实现时直接依赖具体类型。

### 端点拆分

按内聚资源边界拆，出现以下差异即应拆分：

资源归属不同 / 路径层级不同 / 授权上下文不同 / 读写特征不同 /
消费者不同 / 服务依赖不同 / 预期速率或生命周期不同

典型拆分：角色生命周期 vs 角色成员；菜单管理 vs 当前用户导航；
权限定义 vs 角色权限分配；用户资料 vs 认证凭据。

方法数不是唯一标准，但**超过约 7 个内聚操作就应触发边界复审**。

## 第四步：定契约

### 方法签名

- 优先静态工厂 `of()` / `create()` / `from()` / `named()` 而非 public 构造器
- 返回不可变集合（`List.copyOf` / `Map.copyOf`），不暴露内部可变引用
- `Optional<T>` 仅用于**应用侧单值结果**且缺失是预期结果
- `Optional` **禁止**用于参数、字段、record 组件、集合元素、集合返回值
- 简单数据载体用 record

### 空值与校验

| 位置 | 规则 |
|------|------|
| 必填为空/非法 | 抛 `NexusException` + 对应 `StatusCode` |
| 可选参数 | `null → 默认值` 或 `null → 跳过` |
| 集合结果 | 返回不可变空集合，**不得返回 null** |
| 嵌套缺失 | 禁止 `Optional<List<T>>`，无值时返回空列表 |
| DAO 可空返回 | 在 Javadoc 中声明，并在 operator/service 边界归一化或拒绝 |

不得用 `Objects.requireNonNull` / `IllegalArgumentException` /
`NullPointerException` / `IllegalStateException` 表达调用方或业务校验。
纯底层工具对程序员误用可保留 JDK 前置异常，但一旦可能逃逸成应用结果就必须在边界翻译。

校验归属：record 紧凑构造器管不变量 → 请求 `validate()` 管字段组合 →
operator 管数据前置条件与映射缺失 → service 管工作流/授权/跨记录 →
endpoint 只管 Jakarta REST 绑定无法表达的传输层问题。

### 异常与状态码

状态放最窄的拥有方：

- 平台级可复用失败 → base `NexusStatusCode`
- 领域业务失败 → 该域 `<domain>.domain.enums`
- 技术状态 → 发出它的技术边界旁（如 `core.plugin.status.PluginStatusCode`）

全码 = `MODULE(3 大写) + CATEGORY(2) + LOCAL(4)`，共 9 字符。
新增状态码前先搜索现有目录。详见 `java:reference` 的速查表与
`standards/exception-status-code.md`。

### 事务

- 只用 `jakarta.transaction.Transactional`
- 方法级优先，落在最小写操作上，不默认类级
- 多 DAO 写入或跨表协调必须声明事务；简单单表读不加
- 跨表写、级联、关系完整性、稳定键传播归事务 service

### 兼容面

以下改动**必须**先出迁移方案，不得作为机械重命名或内部重构的一部分：

public 类型名与签名、REST 路径与字段、枚举值、状态码、表名列名、稳定业务键、
实体 ID 前缀、索引支撑的唯一性、事件类型串、配置键、插件 ID、能力键、序列化字段名。

废弃：`@Deprecated` + Javadoc `@deprecated` 同时使用，指明替代方案与兼容期。

## 设计评审门禁

- [ ] 归属模块与业务域正确，未与相邻域混同
- [ ] 术语在端点/实体/DAO/数据库/测试之间一致
- [ ] 技术主键与稳定业务键已区分，稳定键的不可变性已定义
- [ ] 分层依赖方向正确，无 operator→service、无 endpoint→dao
- [ ] request/vo 是 record，集合已防御拷贝
- [ ] 接口引入有真实边界理由，非为 mock
- [ ] 端点边界内聚，约 7 个方法已触发复审
- [ ] 每个应用可见失败都有归属明确的状态码
- [ ] 事务边界、幂等、重复调用、生命周期已定义
- [ ] 并发访问策略已声明
- [ ] 兼容面影响已识别并给出迁移或兼容方案
- [ ] 未创建空分层、占位类型、投机性 model/event 包

## 详细参考

- [domain-modeling.md](references/domain-modeling.md) — 实体/请求/VO/模型/枚举/事件的设计决策
- [api-contract.md](references/api-contract.md) — 方法签名、不可变性、空值、事务、并发与兼容性
- [architecture-decision.md](references/architecture-decision.md) — 架构与技术选型决策记录模板
