# 模块归属决策

回答「这块能力归哪个 Maven 模块、哪个业务域」。设计第一步；未完成不得进入
`java:develop` 写源文件。

工程结构权威：`AGENTS.md`、`java:project` → `module-layout.md`。

---

## 依赖方向（不可违反）

```text
base → core → plugin → console → kernel
                              ↘ platform
```

`kernel` 与 `platform` 平行且**互不依赖**。需要协作时：

- 业务中立契约下沉 `console` 或 `core`
- 或由可同时依赖两者的 application/adapter 模块编排
- **不得**把具体业务事件或状态码塞进 core 只为绕过依赖规则

---

## 归属判定表

| 判定 | 归属模块 |
|------|---------|
| 业务中立、依赖轻量的通用能力（异常、状态码、JSON、ID、加解密、HTTP、Snapshot） | `innospots-nexus-base` |
| 业务中立的中间件/数据库/平台基础设施（持久化基类、Quartz、server、watcher、bootstrap、资源元数据） | `innospots-nexus-core`（API 索引见 `references/modules/innospots-nexus-core/README.md`） |
| 插件运行时、contribution 解码、Page DSL、插件安装与能力路由 | `innospots-nexus-plugin` |
| 管理台 REST 契约、VO、converter；**console catalog 索引**（`nx_console_catalog_resource`） | `innospots-nexus-console`（API 索引见 `references/modules/innospots-nexus-console/README.md`） |
| 认证、用户、角色、权限、菜单、字典、审计、工作区/项目业务 | `innospots-nexus-kernel` |
| 租户生命周期、企业主体、平台用户、平台审计 | `innospots-nexus-platform` |
| 业务专属基础设施 | 所属业务模块，或独立 adapter / plugin / extension / application 模块 |

### core、plugin 与 console 易混点

| 能力 | 归属 | 不属于 |
|------|------|--------|
| 插件发现、安装、贡献解析、Page DSL | **plugin** | core、console |
| Catalog 持久化索引与同步 | **console** | core、plugin |
| 插件规范与 contribution 约束定义 | **plugin** | console |
| 持久化基类、审计填充、ID 生成 | **core** | plugin |

---

## 业务域包结构（kernel / platform）

业务代码按**域优先**，再按职责分包（`role/endpoint`、`role/dao`，**禁止**
`endpoint/role`、`dao/role`）。完整规则见 [package-structure.md](package-structure.md)。

```text
<domain>/
  ├── <subcapability>/     # 功能子模块（authorization、grant、entry …，按需）
  │   └── endpoint / service / operator / domain/…
  ├── endpoint
  ├── dao
  ├── domain/{entity,request,vo,model,enums,event}
  ├── converter
  ├── operator / service（按需；单包 ≤15 类）
  ├── handler / listener / interceptor（按需）
```

不得为凑结构创建空包或 `impl` / `common` / `misc` / `util` 子包。
不得在模块根用 `service` 承载多领域业务；`service` 不是全部逻辑的默认归宿。

---

## 阶段零门禁（design → develop 硬边界）

进入 `java:develop` 六阶段之前，必须已在 `java:design` 完成：

1. **归属** — 上表判定完成；相邻域已列出；有意推迟的行为已记录
2. **词汇** — 主概念、技术 ID 与稳定业务键、`state/status/mode/type` 含义、失败归属
3. **边界** — 分层、端点拆分、是否引入接口（见 `domain-modeling.md`）
4. **契约** — 方法签名、空值、异常、事务、兼容面（见 `api-contract.md`）

遗留工程**只作行为参考**，禁止复制源码、POM 或机械复刻包结构。

---

## 归属评审门禁

- [ ] Maven 模块与业务域正确，未与相邻域混同
- [ ] 依赖方向未违反（含 kernel/platform 不互依）
- [ ] plugin / console / core 边界未混淆
- [ ] 术语在端点/实体/DAO/数据库/测试间一致
- [ ] 初始面保持最小（无投机性 model/event/空分层）
