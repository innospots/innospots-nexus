# 外部产品工程：模块语义与 Nexus 依赖挂接

> **适用范围：** `innospots-nexus` **仓库之外**的产品工程（例如 `nexmux`）。
> **不适用：** 修改本仓库 `innospots-nexus-*` 平台库 — 见
> [module-layout.md](module-layout.md) 与根 [`AGENTS.md`](../../../../AGENTS.md)。

## 两种场景

| 场景 | Agent 行为 |
|------|-----------|
| **新建外部产品工程** | 只创建下文「标准模块集合」内的模块；**禁止**照搬 Nexus 的 `portal`/`platform` 等产品侧模块名 |
| **模块结构已确认的产品** | 以现有 `<modules>` 为准，**不得**擅自新增模块；仅改 POM、挂 Nexus 依赖 |

---

## 新建工程：先问用户（两类问题）

动手创建模块前 **必须询问开发者**（未答复不得默认）：

1. **产品形态**
   - 仅 **管理平台**（管理端 + 通常有前端）
   - 仅 **对外接口服务**（无管理台）
   - **两者都要**（管理端 + 对外开放 API，分模块边界）

2. **运行框架**：**Spring Boot** 或 **Quarkus**（**二选一**，禁止两套都建）

---

## 新建工程：标准模块类型（前缀 `{product}-`）

| 模块 | 何时需要 | 说明 |
|------|---------|------|
| **bom** | 始终 | import `innospots-nexus-bom` |
| **core** | 始终 | 产品内共享领域库 |
| **console** | 有管理平台时 | 管理端后端（库模块） |
| **service** | 有对外接口时 | 对外开放 API（库模块） |
| **ui** | 有管理平台且需要前端时 | 随 **console** 出现；纯对外服务 **不要** 建 ui |
| **spring** 或 **quarkus** | 始终（二选一） | 运行时聚合 + 可运行子模块 |

### 按产品形态裁剪（不要多建模块）

| 用户选择 | 应创建的库模块 | 应省略 |
|---------|---------------|--------|
| **仅对外接口服务** | bom、core、**service** | **不要** console、**不要** ui |
| **仅管理平台** | bom、core、**console**、**ui**（要前端时） | 若无对外开放 API，**不要** service |
| **管理平台 + 对外服务** | bom、core、console、service、ui（要前端时） | — |

可运行子模块与形态对齐（示例，Spring）：

- 仅对外服务：`{product}-spring-service`（或 quarkus 同名）
- 仅管理台：`{product}-spring-console` + ui
- 两者：`{product}-spring-console` 与 `{product}-spring-service` + ui

**禁止**同时创建 `{product}-spring` 与 `{product}-quarkus`。  
**不要**创建 `{product}-portal`、`{product}-platform` 等 Nexus 平台库同名产品模块。

### 新建工程示意

**仅对外接口（Spring）：**

```text
{product}/
├── {product}-bom
├── {product}-core
├── {product}-service
└── {product}-spring/
    └── {product}-spring-service
```

**管理平台 + 对外服务（Spring）：**

```text
{product}/
├── {product}-bom
├── {product}-core
├── {product}-console
├── {product}-service
├── {product}-ui
└── {product}-spring/
    ├── {product}-spring-console
    └── {product}-spring-service
```

---

## 模块语义与 Nexus **依赖**（非产品模块名）

Nexus 以 **Maven 依赖**挂到产品**已有**模块上：

| 产品模块 | 语义 | 典型 Nexus artifact（最小依赖） |
|---------|------|--------------------------------|
| `{product}-bom` | 版本清单 | import `innospots-nexus-bom` |
| `{product}-core` | 共享库 | `innospots-nexus-base` / 必要时 `core` |
| `{product}-console` | 管理平台 | `innospots-nexus-console`；租户扩展 `innospots-nexus-portal`；运营 `innospots-nexus-platform`（分模块或分进程；**禁止** portal↔platform 库互依） |
| `{product}-service` | 对外接口 | `innospots-nexus-service-*` 等 |
| `{product}-ui` | 前端 | 通常不直接依赖 Nexus Java 库 |
| `{product}-spring-*` 或 `{product}-quarkus-*` | 可运行组装 | `innospots-nexus-spring-portal`、`spring-platform`、`spring-service` 或 Quarkus 对应组装 + 产品库模块 |

---

## parent 与 BOM（硬性约定）

| 项 | 约定 |
|----|------|
| 产品内一级 **Java** 模块 | `<parent>` = **`innospots-nexus-parent`** |
| `{product}-bom` | `dependencyManagement` **import** **`innospots-nexus-bom`** |
| 禁止 | 模块 POM 内联 JAR `<version>`；禁止复制 `innospots-nexus-bom` 到产品仓库 |

---

## 产品内依赖方向

- `{product}-core` 不依赖 `{product}-console` / `{product}-service`。
- `{product}-console` 与 `{product}-service`：**库层互不依赖**。
- 可运行模块依赖库模块 + 所选框架下的 Nexus 组装 artifact。

---

## 检查清单

**新建工程**

- [ ] 已询问 **产品形态**（仅管理 / 仅对外 / 两者）并按表裁剪 console、service、ui
- [ ] 已询问并确认 **Spring 或 Quarkus 仅一套**
- [ ] 纯对外服务时 **无** console、**无** ui
- [ ] 未创建 Nexus 同名平台库模块（portal/platform 等仅为依赖）
- [ ] grill-me / 产品 `AGENTS.md` 已记录选型

**已有工程**

- [ ] 未擅自新增 Maven 模块
- [ ] `mvn -pl <模块> -am clean compile` 与 `dependency:tree` 通过

交付步骤见 [project-deliverables.md](project-deliverables.md) →「外部产品工程」。
