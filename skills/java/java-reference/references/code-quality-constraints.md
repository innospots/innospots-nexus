# 结构简化、冗余治理与防过度设计

跨技能执行参考，供 `java:design`、`java:develop`、`java:check` 链接复用。
思想对齐「按今天会接受的更简单形状替换旧形状」，**不是**鼓励不断抽象。

规范红线仍见 [quick-constraints.md](quick-constraints.md)；本文件管**复杂度、重复、无效层与 AI 噪声**。

---

## 核心原则

| 原则 | 含义 |
|------|------|
| 一行即负债 | 新增代码必须能回答「谁调用、删了会怎样」；答不出则不写 |
| 一概念一 owner | 同一业务能力只在一个边界拥有实现与失败语义 |
| 先查再建 | 写新类型/新包前搜索同义 endpoint、operator、状态码、转换器 |
| 替换而非并存 | 迁移完成后删除旧路径、旧别名、兼容 wrapper；不为「开发期方便」长期双轨 |
| 共享原语优于 N 个适配器 | 框架/BOM/base/core 已有能力不得再包一层「项目专用」壳 |
| 小需求大 diff 是 smell | 行为变化很小却新增/改动数百行 → 先重新审视结构，再继续堆代码 |

---

## 增加代码前的门禁（无具体答案 → 不写）

在 design 定方案、develop 写实现、check 评审 diff 时，对**每一类新增**自问：

| 问题 | 过不了则 |
|------|---------|
| 平台/依赖/BOM 是否已提供同等能力？ | 用现有 API，不新建 `XxxHelper` / `XxxAdapter` |
| 失败场景是否已有调用方或测试证明？ | 不写「以防万一」的 catch、默认值、空实现 |
| 删除这段校验/分支后，是否有真实 bug 或契约违反？ | 删除过度防御 |
| 接口是否满足 api-design 四类引入条件（非仅为 mock）？ | 用具体类 + 构造器注入 |
| 新 Maven 依赖是否已在 design 选型并登记 BOM？ | 回 design 或删依赖 |
| 新包/新分层是否承载 ≥1 个非转发职责？ | 合并到已有层或延后建包 |

---

## 结构整理（refactor-clean 式）

适用：**重构**、**大段 AI 生成后的收敛**、**check 发现的结构性警告**。

```text
定目标（外部可见行为不变，或已有迁移方案）
    ↓
锁行为（测试；缺则 develop 先补）
    ↓
列候选（重复、wrapper、双轨、错层）
    ↓
调用关系确认（见「无效代码删除」）
    ↓
小步替换 → 删旧路径 → mvn clean compile → mvn test
    ↓
java:check（含本文件清单）
```

| 观察到 | 倾向动作 |
|--------|---------|
| 两处同义查询/校验/映射 | 合并到 owning operator 或 converter |
| 仅转发一行的 service | 删掉 service，endpoint → operator |
| `*Impl` 且无第二实现 | 内联为具体类 |
| Interface + 单实现「为了测试」 | Mockito 测具体类或测 owning 边界 |
| Utils/Common/Base 静态方法堆 | 收回职责边界或并入已有类型 |
| Request/VO/Entity 字段重复三轮 | 收敛 record 形状，映射集中在 converter |
| 旧 API + 新 API 并存 | 迁移调用方后删除旧 API |
| 文件 >1000 行 | 按**职责**拆，不为压行数机械拆文件 |

**禁止：** 为「好看」引入新抽象；重构中夹带新功能；未确认无引用就删 public 类型。

---

## 无效代码删除（dead-code 式）

删除是**独立工作流**，与功能开发分开，避免「搜不到就删」。

```text
候选（静态分析、IDE、dependency:analyze、重复类型）
    ↓
引用确认（全仓库搜索、反射/配置键、插件贡献、测试、Spring/Quarkus 装配）
    ↓
删除或收窄可见性（package-private、合并测试）
    ↓
mvn clean compile && mvn test
```

| 类型 | 删除前额外确认 |
|------|----------------|
| public 类型/方法 | 模块外、console 契约、插件 SPI |
| 状态码枚举成员 | 告警/文档/客户端是否引用 |
| yaml 配置键 | `@ConfigurationProperties` 与文档 |
| 测试类 | 是否锁定尚未迁移的行为 |

---

## Java / Nexus 特有噪声

| 噪声 | 处理 |
|------|------|
| 多余 DTO 层（`XxxDto`/`Response`/`Result`） | 统一 `domain.vo` record + `R<T>` |
| `Manager`/`Processor`/`Handler` 万能后缀 | 改成语义后缀表（Service/Operator/Registry…） |
| 每层都 `try-catch` 再包装 | 在 owning 边界 `NexusException.build(status, cause)` |
| Stream/Optional 单行炫技 | 直白循环/判空，除非确实简化 |
| 注释掉的实现、AI 废话注释 | 删除；行内注释只留 why |
| 无消费者的 domain event / 空 event 包 | design 复审；无计划则不要建 |
| 投机性 `config`/`properties` 键 | 无读取方则不建 |
| base/core 越权中间件 | 回 [module-ownership.md](module-ownership.md) |

---

## 各技能职责分工

| 技能 | 本主题的职责 |
|------|----------------|
| `java:design` | 方案层禁止空分层、双轨 API、投机事件/接口；L2 写清「不建什么」 |
| `java:develop` | 最小实现；重构走结构整理流程；不加未在设计内的类型 |
| `java:check` | diff 体量与结构 smell；清单 §O；区分阻塞/警告 |

**不是**单独第七个技能：评审时链到本文件即可。

---

## 快速自检（10 条）

- [ ] diff 行数与需求规模是否匹配；过大则先结构复审
- [ ] 无新增仅转发方法/类型
- [ ] 无新 `*Util`/`*Common`/单实现 `*Impl`
- [ ] 无注释掉的大段代码与复述代码的注释
- [ ] 无旧路径与新路径长期并存（或有 dated 迁移计划）
- [ ] 重复逻辑已合并到单一 owner
- [ ] 删除项经过引用确认
- [ ] 未为 mock 单独引入接口层
- [ ] 未复制 legacy 结构「对齐旧工程」
- [ ] 行为不变的重构未削弱测试断言
