# 包结构：领域优先

回答「Java 源码包怎么划、目录怎么长」。与 [module-ownership.md](module-ownership.md)
（Maven 模块归属）和 [module-layout.md](../../java-project/references/module-layout.md)
（工程模块树）配合使用。

**核心规则：业务代码先按领域（domain）划分，再在领域内按技术模块（endpoint、dao、
service、operator、domain、filter、loader 等）划分。技术模块内类型较多或存在清晰、
可独立命名的功能曲面时，再按功能子包分子包；类型不多或功能区分度不大时保持扁平。**

`domain` 固定承载四类数据形状：`entity`、`vo`、`model`、`request`。不可变的数据形态
优先使用 Java `record`；JPA/MyBatis-Plus 实体及确有可变身份的领域类型使用 class。

禁止按技术层把多个领域平铺在同一层下（`dao/role`、`dao/menu`、`endpoint/role` …）。
禁止在模块根下用单个 `service` 包承载全部业务，禁止在一个包内无节制堆积类型。

---

## 两种组织方式对比

### 正确 — 领域优先（Domain-first）

```text
com.innospots.nexus.portal
  ├── role
  │   ├── endpoint
  │   ├── dao
  │   ├── domain
  │   │   ├── entity
  │   │   ├── request
  │   │   ├── vo
  │   │   ├── enums
  │   │   └── event        （按需）
  │   ├── converter        （按需）
  │   ├── operator         （按需）
  │   └── service          （按需）
  ├── menu
  │   ├── endpoint
  │   ├── dao
  │   └── domain
  │       └── ...
  └── permission
      └── ...
```

同一业务概念（如 `role`）的 endpoint、dao、entity、service **聚在一个领域根下**。
打开 `role/` 即可看到该领域的完整垂直切片。

### 错误 — 技术层优先（Layer-first）

```text
com.innospots.nexus.portal
  ├── endpoint
  │   ├── role
  │   ├── menu
  │   └── permission
  ├── dao
  │   ├── role
  │   ├── menu
  │   └── permission
  └── domain
      ├── role
      │   └── entity
      ├── menu
      └── permission
```

这是**禁止**的结构：按 `endpoint` / `dao` / `domain` 把多个领域拆开，
改一个领域要跨多个顶层包跳转，边界模糊，易与相邻领域混用类型。

| 维度 | 领域优先 | 技术层优先（禁止） |
|------|---------|-------------------|
| 第一级包段 | 业务领域名 `role`、`menu` | 技术职责名 `endpoint`、`dao` |
| 查找「角色」全部代码 | 进入 `role/` | 在 `endpoint/role`、`dao/role`、`domain/role` 间跳转 |
| 新增领域 | 新建 `xxx/` 子树 | 在多个顶层包下各建 `xxx/` |
| 与 DDD 对齐 | 有界上下文内聚 | 按基础设施分层，领域被撕裂 |

---

## Maven 模块 vs Java 包

| 层级 | 划分依据 | 示例 |
|------|---------|------|
| **Maven 模块** | 部署边界、依赖方向、可独立测试 | `innospots-nexus-portal`、`innospots-nexus-console` |
| **Java 包（业务模块内）** | 业务领域 → 技术模块 → 按需功能子包 | `portal.role.endpoint`、`portal.role.service.grant` |

- 不要在未证明边界清晰时，为每个技术层再建一个 Maven 模块。
- 在**同一个** Maven 模块内，仍必须**领域优先**，不得用「再拆一个 dao 模块」替代包结构。

技术/非业务模块（`base`、`core`、`plugin`）按**能力/子系统**组织包，不按业务领域；
见 [naming.md](../standards/naming.md) 中 `core.plugin` 示例。

---

## 标准领域包骨架

### 标准领域骨架

业务领域统一使用下面的技术模块划分。包归属固定；只有出现真实类型时才创建目录，
不得为了“结构完整”提交空包或占位类。

```text
<domain>/
  ├── endpoint
  ├── dao
  ├── service
  ├── operator
  ├── domain
  │   ├── entity
  │   ├── vo
  │   ├── model
  │   └── request
  ├── filter          （按需）
  └── loader          （按需）
```

### 附加技术模块

| 包 | 何时添加 |
|----|---------|
| `converter` | 需要 MapStruct 或集中类型转换 |
| `filter` | 需要 JAX-RS/请求链或领域级过滤 |
| `loader` | 需要从 classpath、文件或外部快照加载配置/资源 |
| `handler` / `listener` / `interceptor` | 需要对应运行时角色 |
| `domain/event` | 需要发布领域事件 |
| `domain/enums` | 需要领域枚举或状态码 |
| `api` | 需要向其他模块暴露非 HTTP 端口（少见） |
| `config` | 模块级配置绑定（**领域根下**仅当有领域专属配置） |

模块级共享配置放在 `com.innospots.nexus.<module>.config`，不要塞进某个业务的 `domain` 里。

---

## 功能子包划分（避免 service 堆积）

领域（`role`、`permission`）之下先保留标准技术模块。某个技术模块内类型较多，或包含
边界清楚、可独立命名的功能曲面时，再在该技术模块内划分子包；不要仅因为“以后可能长大”
就预建功能包。

### 分层顺序

```text
Maven 模块（portal / console / platform）
  └── 业务领域（role / permission / catalog）     ← 第一刀：有界上下文
        └── 技术模块（endpoint / service / dao / operator / domain）  ← 第二刀：技术职责
              └── 功能子包（grant / authorization / entry）  ← 第四刀：按需细分
```

- **小领域或功能区分度不大**：保持 `endpoint`、`dao`、`service`、`operator`、`domain`
  扁平结构，不为少量类再建一层功能包。
- **技术模块类型较多或功能区分明显**（例如独立的 grant、authorization、sync 工作流）：
  在对应技术模块下按功能分组，例如 `service/grant`、`endpoint/authorization`、
  `domain/request/grant`。功能子包仍遵循领域归属，不得把领域重新平铺成技术层。

### 正确 — 在技术模块内按功能拆分

```text
com.innospots.nexus.console.permission
  ├── endpoint
  │   └── authorization      # endpoint 较多且功能区分清晰时
  ├── service
  │   ├── grant              # grant 工作流
  │   └── visibility         # visibility 工作流
  ├── dao
  └── domain/
      ├── entity
      ├── model
      ├── request
      └── vo
```

子能力继续长大时，**在对应技术模块内提升为功能子包**，而不是在同一个 `service` 里无限加类：

```text
permission/
  ├── endpoint/
  │   ├── grant/             # 端点较多时按功能拆分
  │   └── visibility/
  ├── service/
  │   ├── grant/
  │   └── visibility/
  ├── operator/
  ├── dao/
  └── domain/
```

功能区分度不大时保持扁平：

```text
permission/
  ├── endpoint/
  ├── service/
  ├── operator/
  ├── dao/
  └── domain/
```

### 错误 — service 垃圾桶

```text
# ✗ 模块级：所有业务塞进一个 service 包
portal.service.RoleService
portal.service.MenuService
portal.service.PermissionGrantService
…（数十个 *Service）

# ✗ 领域级：一个扁平 service 包堆满编排类
permission.service.PermissionGrantService
permission.service.PermissionVisibilityService
permission.service.PermissionResourceSyncService
permission.service.PermissionCacheRefreshService
…（>15 个类仍不分子包）

# ✗ 用 service 替代功能划分：本应有 grant/authorization 功能子包，却全部叫 XxxService
```

| 信号 | 动作 |
|------|------|
| 模块根出现 `service/` 且下面挂多个领域 | **禁止**；按领域拆到 `role/`、`menu/` 等 |
| 技术模块内类数接近 15 且职责可分组 | 在技术模块下按功能拆分 |
| 领域内 `service/` 接近或超过 15 个类 | **必须**拆分子包，不得继续平铺 |
| 鉴权、同步等有清晰独立边界且与 CRUD 编排混在一起 | 在对应技术模块下抽出 `service/authorization`、`service/sync` 等功能子包 |
| `endpoint` / `domain/request` 同类文件过多 | 按子功能拆 `endpoint/grant/`、`domain/request/grant/` 等 |

`service` 包的含义是**工作流编排**，不是「所有业务逻辑的唯一归宿」。能放进
`operator`、领域模型行为、`service/authorization` 等更窄边界的逻辑，不要默认再建一个 `*Service`。

---

## 单包类型数量上限（15）

**同一包目录下**（仅统计该目录内直接的 `*.java` 文件，不含子目录）：

| 数量 | 要求 |
|------|------|
| ≤ 12 | 正常 |
| 13～15 | 允许，但新增类型前应规划子包拆分 |
| **> 15** | **禁止**；必须先拆分子包再合并 PR |

适用对象：`endpoint`、`service`、`operator`、`dao`、`domain.request`、`domain.vo`、
技术模块内的功能子包根，以及技术模块中的任意包。

拆分顺序建议：

1. 保持**领域 → 技术模块**的固定层级（`permission.service`、`permission.endpoint`）。
2. 技术模块仍过大时，在该技术模块内按功能拆分子包（`service/grant`、`endpoint/visibility`、
   `domain/request/grant`）。
3. 禁止用 `common`、`misc`、`util` 逃避拆分。

自检命令（本地）：

```bash
find <module>/src/main/java/<package/path> -maxdepth 1 -name '*.java' | wc -l
```

---

## 职责包含义（领域内）

| 包名 | 内容 | 类型后缀 |
|------|------|---------|
| `endpoint` | Jakarta REST HTTP 边界 | `*Endpoint` |
| `dao` | MyBatis-Plus 单表 mapper | `*Dao` |
| `domain.entity` | 持久化实体 | `*Entity` |
| `domain.request` | 入参 record | `*Request` |
| `domain.vo` | 出参 record | `*Vo` |
| `domain.model` | 内部业务模型；不可变时优先 record | 无强制后缀 |
| `domain.enums` | 枚举、领域状态码 | 枚举 / `*StatusCode` |
| `domain.event` | 领域事件 | `*Event` |
| `converter` | MapStruct 转换 | `*Converter` |
| `operator` | 单表/直接数据操作 | `*Operator` |
| `service` | 工作流与编排；**单包 ≤15 类**，多则在 `service/<sub>/` 按功能拆分 | `*Service` |
| `filter` | 请求链或领域级过滤 | `*Filter` |
| `loader` | 资源、配置或快照加载 | `*Loader` |
| `<subcapability>` | 技术模块内功能子包（`authorization`、`grant`、`sync` …） | 按实际功能命名 |
| `api` | 非 HTTP 对外契约 | 接口，能力命名 |

完整命名规则见 [standards/naming.md](../standards/naming.md)。

### 职责包之间的依赖方向（强制）

```text
endpoint → service → operator → dao
```

- **operator 只能依赖 dao**（外加无状态 converter 与共享底层技术工具）；
  禁止依赖 service 或另一个 operator。
- **service 可依赖 operator、其他 service 和 dao**；跨 service 协调不得形成循环依赖。
- 允许简化：`endpoint → operator → dao`、`service → dao`；endpoint 不得直接编排 DAO。

---

## `domain` 子包（固定约定）

`domain` **不是**与 `role` 平级的「又一个领域名」，而是**领域内**存放数据形状的子包：

```text
role/domain/entity/RoleEntity.java      ✓
role/domain/request/RoleCreateRequest.java   ✓

domain/role/entity/RoleEntity.java      ✗  技术层优先 + 错误嵌套
role/entity/RoleEntity.java             ✗  缺少 domain 层
```

标准 `domain` 子包固定为 `entity`、`vo`、`model`、`request`；按需增加 `enums`、`event`。
`domain.enums` 是项目明确约定的**复数**例外。
不可变 request/vo/model 优先使用 record；持久化实体及确有可变身份/生命周期的 model
使用 class。

---

## 禁止的包结构

| 禁止 | 说明 |
|------|------|
| `endpoint/<domain>/` | 技术层优先 |
| `dao/<domain>/` | 技术层优先 |
| `domain/<domain>/` | 把领域名塞进全局 `domain` 包 |
| `service/<domain>/` | 技术层优先 |
| `<module>/service/`（模块根下挂多领域 Service） | 业务 service 垃圾桶 |
| 单包 > 15 个 `.java` 仍不分子包 | 必须在对应技术模块内按功能拆分 |
| `impl/`、`common/`、`misc/`、`util/` 子包 | 掩盖归属；用精确职责包或模块级 `util` |
| 无类型的空包 | 仅为「分层好看」 |
| 跨领域共享的「大杂烩」包 | 应下沉 `base`/`core`/`console` 契约或抽端口 |

---

## 跨领域协作

- **不要**为了调用方便把 A 领域的类放进 B 领域的包。
- 共享契约下沉：`innospots-nexus-base`（轻量）、`innospots-nexus-console`（管理台契约）、
  `innospots-nexus-core`（平台基础设施）。
- 同一 Maven 模块内两领域协作：通过 **service** 编排或 **领域事件**，而不是合并包树。
- `portal` 与 `platform` **不得** Maven 互依；需要同时暴露时用 application 组装模块
  （见 [dependency-conventions.md](../../java-project/references/dependency-conventions.md)）。

---

## 与控制台模块的对应关系

`innospots-nexus-console` 已按领域优先组织，可作为参考：

```text
com.innospots.nexus.console
  ├── auth
  │   ├── endpoint / api / service / operator / dao / domain
  ├── permission
  │   ├── endpoint / dao / domain
  │   ├── service
  │   │   ├── authorization    # 请求鉴权（类多时才拆）
  │   │   └── pageaccess       # 页面访问（类多时才拆）
  ├── catalog
  │   └── service
  │       └── bootstrap    # 功能子包：启动同步
  ├── dictionary
  │   ├── endpoint / service / operator / dao / domain
  │   └── converter
  │       └── json         # 功能子包：序列化转换
  ├── role / menu / …
  └── entry                # 模块级装配（非业务 service 桶）
```

新建 portal/platform 领域时，**对齐同一模式**：`com.innospots.nexus.portal.<domain>.<responsibility>`。

---

## 平台扩展库（交付面优先）

适用：`innospots-nexus-sample-platform` 及同类 **在 platform 之上的无框架扩展 JAR**（非 `innospots-nexus-platform` 本体）。

在扩展库内 **第一级为交付面、第二级为领域、第三级为技术模块**；技术模块较大时再按功能拆子包：

```text
com.innospots.nexus.sample.platform
  ├── core.<domain>/{dao,domain,operator,service,loader}
  │   └── service.<function>（按需）
  ├── console.<domain>/{endpoint,service}
  │   └── endpoint.<function>（按需）
  └── inbound.<domain>/{endpoint,service,…}
      └── service.<function>（按需）
```

| 交付面 | 禁止 |
|--------|------|
| `core` | Jakarta REST `endpoint`、Spring/Quarkus 类型 |
| `console` / `inbound` | `dao`、`domain.entity`（应引用 `core` 类型） |
| 任意 | `inbound` → `console` 包依赖；模块根多领域 `service` 桶 |

**与平台库并存：** `com.innospots.nexus.platform.tenant.endpoint` 仍是领域优先；
扩展库多一刀交付面是为路径与装配隔离，**领域内**仍禁止 `endpoint/<domain>` 式技术层优先。

完整工程、路径前缀、Maven 模块树见
[sample-extension-layout.md](sample-extension-layout.md)。

---

## 自检清单

新建或调整包结构前确认：

- [ ] 第一级业务包段是**领域名**（`role`），不是技术层名（`dao`）？
- [ ] 模块根下**没有**承载多领域业务的 `service/` 包？
- [ ] `endpoint`、`dao`、`domain` 都在该领域目录**内部**？
- [ ] 技术模块内多个独立功能曲面是否已拆为**功能子包**（而非全堆在 `service`）？
- [ ] 每个包目录内 `.java` 数量 **≤ 15**（含 `package-info.java`）？
- [ ] `domain` 使用约定子包（`entity`/`vo`/`model`/`request`，按需 `enums`/`event`）？
- [ ] 不可变数据形态优先使用 record，持久化实体保持可变 class？
- [ ] 没有 `impl`、`common`、`misc` 逃避归属？
- [ ] 初始面是否最小（未投机性创建 `service`/`event`/空包）？
- [ ] 共享类型是否已放到正确的 **Maven 模块** 而非塞进邻近领域包？
- [ ] 若为 **sample 扩展库**：交付面（core/console/inbound）与领域二级包是否正确？`core` 是否未放 endpoint？

疑义回源：[standards/naming.md](../standards/naming.md)「包命名」、
[standards/domain-module-initialization.md](../standards/domain-module-initialization.md) 阶段零。
