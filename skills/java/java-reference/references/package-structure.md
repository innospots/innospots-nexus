# 包结构：领域优先

回答「Java 源码包怎么划、目录怎么长」。与 [module-ownership.md](module-ownership.md)
（Maven 模块归属）和 [module-layout.md](../../java-project/references/module-layout.md)
（工程模块树）配合使用。

**核心规则：业务代码先按领域（domain）划分，再在领域内按职责（endpoint、dao、domain 等）划分；
职责包过大时再按功能子模块（grant、authorization、entry …）分子包。**

禁止按技术层把多个领域平铺在同一层下（`dao/role`、`dao/menu`、`endpoint/role` …）。
禁止在模块根下用单个 `service` 包承载全部业务，禁止在一个包内无节制堆积类型。

---

## 两种组织方式对比

### 正确 — 领域优先（Domain-first）

```text
com.innospots.nexus.kernel
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
com.innospots.nexus.kernel
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
| **Maven 模块** | 部署边界、依赖方向、可独立测试 | `innospots-nexus-kernel`、`innospots-nexus-console` |
| **Java 包（业务模块内）** | 业务领域 → 职责 | `kernel.role.endpoint`、`kernel.role.dao` |

- 不要在未证明边界清晰时，为每个技术层再建一个 Maven 模块。
- 在**同一个** Maven 模块内，仍必须**领域优先**，不得用「再拆一个 dao 模块」替代包结构。

技术/非业务模块（`base`、`core`、`plugin`）按**能力/子系统**组织包，不按业务领域；
见 [naming.md](../standards/naming.md) 中 `core.plugin` 示例。

---

## 标准领域包骨架

### 初始最小面（新领域）

仅当当前任务需要时才加包；不要预建空目录。

```text
<domain>/
  ├── dao
  ├── domain/
  │   ├── entity
  │   ├── enums
  │   ├── request
  │   └── vo
  └── endpoint
```

### 按需扩展

| 包 | 何时添加 |
|----|---------|
| `converter` | 需要 MapStruct 或集中类型转换 |
| `operator` | 需要直接面向 DAO 的数据操作 |
| `service` | 需要工作流、编排、跨记录事务 |
| `domain/model` | 需要与 entity/request/vo 分离的内部模型 |
| `domain/event` | 需要发布领域事件 |
| `handler` / `listener` / `interceptor` | 需要对应运行时角色 |
| `api` | 需要向其他模块暴露非 HTTP 端口（少见） |
| `config` | 模块级配置绑定（**领域根下**仅当有领域专属配置） |

模块级共享配置放在 `com.innospots.nexus.<module>.config`，不要塞进某个业务的 `domain` 里。

---

## 功能子模块划分（避免 service 堆积）

领域（`role`、`permission`）之下，除标准职责包外，**必须**按可识别的功能曲面划分子包，
不要把同一领域内的全部编排逻辑塞进一个扁平 `service` 目录。

### 三层结构

```text
Maven 模块（kernel / console / platform）
  └── 业务领域（role / permission / catalog）     ← 第一刀：有界上下文
        └── 功能子模块（grant / authorization / entry）  ← 第二刀：领域内功能曲面（按需）
              └── 职责包（endpoint / service / dao / domain/…）  ← 第三刀：技术职责
```

- **小领域**（预计整个领域 < 15 个类型）：可只在领域根下挂 `endpoint`、`dao`、`domain`，
  `service` 内 1～3 个编排类即可。
- **中/大领域**（多个独立工作流、鉴权、同步、插件入口等）：在领域根下增加**功能子模块包**，
  每个子模块自带需要的职责子树。

### 正确 — 按功能子模块拆分

```text
com.innospots.nexus.console.permission
  ├── authorization          # 请求鉴权（与 grant 编排分离）
  │   ├── RequestAuthorizer.java
  │   └── AuthorizationContext.java
  ├── entry                  # 控制台插件入口
  │   └── PermissionEntryPlugin.java
  ├── endpoint               # 领域级 HTTP 边界（可再按子功能拆 endpoint 子包）
  ├── service                # 仅保留少量领域级编排（≤15 类/包）
  │   ├── PermissionGrantService.java
  │   └── PermissionVisibilityService.java
  ├── dao
  └── domain/
      ├── entity
      ├── request
      └── vo
```

子能力继续长大时，**优先把子能力提升为功能子模块**，而不是在同一个 `service` 里无限加类：

```text
permission/
  ├── grant/                 # 授权授予子模块
  │   ├── service/
  │   ├── operator/
  │   └── domain/...
  ├── visibility/
  │   └── service/
  ├── resource/              # 权限资源同步
  │   └── service/
  └── endpoint/              # 或 grant/endpoint、visibility/endpoint
```

### 错误 — service 垃圾桶

```text
# ✗ 模块级：所有业务塞进一个 service 包
kernel.service.RoleService
kernel.service.MenuService
kernel.service.PermissionGrantService
…（数十个 *Service）

# ✗ 领域级：一个扁平 service 包堆满编排类
permission.service.PermissionGrantService
permission.service.PermissionVisibilityService
permission.service.PermissionResourceSyncService
permission.service.PermissionCacheRefreshService
…（>15 个类仍不分子包）

# ✗ 用 service 替代功能划分：本应有 grant/authorization 子模块，却全部叫 XxxService
```

| 信号 | 动作 |
|------|------|
| 模块根出现 `service/` 且下面挂多个领域 | **禁止**；按领域拆到 `role/`、`menu/` 等 |
| 领域内 `service/` 已有 ≥ 5 个类且职责可分组 | 按功能子模块或 `service/<subcapability>/` 拆分 |
| 领域内 `service/` 接近或超过 15 个类 | **必须**拆分子包，不得继续平铺 |
| 鉴权、同步、入口注册等与 CRUD 编排混在一起 | 抽出 `authorization`、`sync`、`entry` 等功能子包 |
| `endpoint` / `domain/request` 同类文件过多 | 按子功能拆 `endpoint/grant/`、`domain/request/grant/` 等 |

`service` 包的含义是**工作流编排**，不是「所有业务逻辑的唯一归宿」。能放进
`operator`、领域模型行为、`authorization` 等更窄边界的逻辑，不要默认再建一个 `*Service`。

---

## 单包类型数量上限（15）

**同一包目录下**（仅统计该目录内直接的 `*.java` 文件，不含子目录）：

| 数量 | 要求 |
|------|------|
| ≤ 12 | 正常 |
| 13～15 | 允许，但新增类型前应规划子包拆分 |
| **> 15** | **禁止**；必须先拆分子包再合并 PR |

适用对象：`endpoint`、`service`、`operator`、`dao`、`domain.request`、`domain.vo`、
功能子模块根包，以及技术模块中的任意包。

拆分顺序建议：

1. 先按**功能子模块**拆到领域下一级（`permission.grant`、`permission.visibility`）。
2. 子模块仍过大时，在职责包下再拆（`grant.service`、`grant.domain.request`）。
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
| `domain.model` | 内部业务模型 | 无强制后缀 |
| `domain.enums` | 枚举、领域状态码 | 枚举 / `*StatusCode` |
| `domain.event` | 领域事件 | `*Event` |
| `converter` | MapStruct 转换 | `*Converter` |
| `operator` | 单表/直接数据操作 | `*Operator` |
| `service` | 工作流与编排；**单包 ≤15 类**，多则按功能子模块或 `service/<sub>/` 拆分 | `*Service` |
| `<subcapability>` | 领域内功能子模块（`authorization`、`grant`、`entry`、`sync` …） | 按实际角色命名 |
| `api` | 非 HTTP 对外契约 | 接口，能力命名 |

完整命名规则见 [standards/naming.md](../standards/naming.md)。

---

## `domain` 子包（固定约定）

`domain` **不是**与 `role` 平级的「又一个领域名」，而是**领域内**存放数据形状的子包：

```text
role/domain/entity/RoleEntity.java      ✓
role/domain/request/RoleCreateRequest.java   ✓

domain/role/entity/RoleEntity.java      ✗  技术层优先 + 错误嵌套
role/entity/RoleEntity.java             ✗  缺少 domain 层（request/vo 无处安放）
```

允许的 `domain` 子包：`entity`、`request`、`vo`、`model`、`enums`、`event`。
`domain.enums` 是项目明确约定的**复数**例外。

---

## 禁止的包结构

| 禁止 | 说明 |
|------|------|
| `endpoint/<domain>/` | 技术层优先 |
| `dao/<domain>/` | 技术层优先 |
| `domain/<domain>/` | 把领域名塞进全局 `domain` 包 |
| `service/<domain>/` | 技术层优先 |
| `<module>/service/`（模块根下挂多领域 Service） | 业务 service 垃圾桶 |
| 单包 > 15 个 `.java` 仍不分子包 | 必须按功能子模块或职责子包拆分 |
| `impl/`、`common/`、`misc/`、`util/` 子包 | 掩盖归属；用精确职责包或模块级 `util` |
| 无类型的空包 | 仅为「分层好看」 |
| 跨领域共享的「大杂烩」包 | 应下沉 `base`/`core`/`console` 契约或抽端口 |

---

## 跨领域协作

- **不要**为了调用方便把 A 领域的类放进 B 领域的包。
- 共享契约下沉：`innospots-nexus-base`（轻量）、`innospots-nexus-console`（管理台契约）、
  `innospots-nexus-core`（平台基础设施）。
- 同一 Maven 模块内两领域协作：通过 **service** 编排或 **领域事件**，而不是合并包树。
- `kernel` 与 `platform` **不得** Maven 互依；需要同时暴露时用 application 组装模块
  （见 [dependency-conventions.md](../../java-project/references/dependency-conventions.md)）。

---

## 与控制台模块的对应关系

`innospots-nexus-console` 已按领域优先组织，可作为参考：

```text
com.innospots.nexus.console
  ├── auth
  │   ├── endpoint / api / service / domain
  ├── permission
  │   ├── authorization    # 功能子模块：请求鉴权
  │   ├── entry            # 功能子模块：插件入口
  │   ├── endpoint / service / dao / domain
  ├── catalog
  │   └── bootstrap        # 功能子模块：启动同步
  ├── dictionary
  │   └── entry
  ├── role / menu / …
  └── entry                # 模块级装配（非业务 service 桶）
```

新建 kernel/platform 领域时，**对齐同一模式**：`com.innospots.nexus.kernel.<domain>.<responsibility>`。

---

## 自检清单

新建或调整包结构前确认：

- [ ] 第一级业务包段是**领域名**（`role`），不是技术层名（`dao`）？
- [ ] 模块根下**没有**承载多领域业务的 `service/` 包？
- [ ] `endpoint`、`dao`、`domain` 都在该领域目录**内部**？
- [ ] 领域内多个独立功能曲面是否已拆为**功能子模块**（而非全堆在 `service`）？
- [ ] 每个包目录内 `.java` 数量 **≤ 15**（含 `package-info.java`）？
- [ ] `domain` 下仅使用约定的子包（`entity`/`request`/`vo`/…）？
- [ ] 没有 `impl`、`common`、`misc` 逃避归属？
- [ ] 初始面是否最小（未投机性创建 `service`/`event`/空包）？
- [ ] 共享类型是否已放到正确的 **Maven 模块** 而非塞进邻近领域包？

疑义回源：[standards/naming.md](../standards/naming.md)「包命名」、
[standards/domain-module-initialization.md](../standards/domain-module-initialization.md) 阶段零。
