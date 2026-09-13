# 工程交付物规范

`java:project` 的产出是 **Maven 结构与 POM 变更**，不是业务源码或设计文档。
业务包结构由 `java:design` 定，实现由 `java:develop` 完成（见
[develop-deliverables.md](../../java-develop/references/develop-deliverables.md)）。

全量规范评审由 `java:check` 执行；`${revision}` 升版用 `java:project-upgrade`；
第三方/JDK 升版用 `java:dependency-upgrade`。

---

## 上游前置

| 操作 | 是否必经 grill-me | 说明 |
|------|-------------------|------|
| 新建 Maven 模块、改 reactor、改依赖方向 | **是** | 见 [grill-me.md](../../java-reference/references/grill-me.md)；未安装则 `npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"` |
| 拆分/合并模块、新建 application/adapter | **是** | 结论写入 PR 或 `docs/design/adr/` |
| 纯 POM 版本对齐、单模块插件微调 | 否 | 仍须遵守 BOM，不得内联 version |
| 写 Java 业务类 | — | **禁止**；交 `java:develop` |

**找不到 grill-me 结论（结构变更类）** → 不得改 reactor / 注册新模块。

---

## 何时新建 Maven 模块 vs 何时只加 Java 包

**默认：不新建 Maven 模块。** 先在现有 `kernel` / `platform` / `console` 内按领域加包
（见 [package-structure.md](../../java-reference/references/package-structure.md)、
[develop-deliverables.md](../../java-develop/references/develop-deliverables.md)）。

仅当 **边界清晰 + 依赖方向单向 + 可独立测试** 三者同时满足，才新建 Maven 模块。

| 信号 | 动作 |
|------|------|
| 只是新业务域（role、menu…） | **不建** Maven 模块 → `kernel` 下新领域包 → `java:design` |
| 单领域类型将超 40～50 且无子模块规划 | 先 `java:design` 拆功能子模块（仍是 Java 包） |
| 需同时依赖 kernel 与 platform | **application 组装模块**，禁止 kernel↔platform 互依 |
| 外部系统 / 客户专属集成 | **adapter** 模块 |
| classpath 插件、不经管理台表 | **plugin** 边界或独立插件工程 |
| 为 dao/service/endpoint 各建 Maven 子模块 | **禁止** |

---

## Maven 模块类型决策表

| 类型 | 何时建 | 典型 artifact | parent | 直接依赖（示例） |
|------|--------|---------------|--------|------------------|
| **库模块（业务）** | 极少；多数能力应落在 kernel/platform **包**内 | `innospots-nexus-kernel`（已有） | `innospots-nexus-parent` | 场景见 dependency-conventions |
| **adapter** | 外部 API、客户专属基础设施、隔离第三方 SDK | `innospots-nexus-*-adapter` | parent | `console` 或 `core` + 外部库（BOM 登记） |
| **application** | 同进程组装 kernel+platform 或可执行 JAR 入口 | `innospots-nexus-spring-*` 下新子模块 | spring/quarkus 聚合或 parent | `*-app` + `kernel` + `platform` |
| **运行时子模块** | Spring/Quarkus 可运行服务 | `spring-console`、`spring-app` | `innospots-nexus-spring` | 见 [dependency-conventions.md](dependency-conventions.md) |
| **plugin 扩展** | 插件运行时、贡献、非 console catalog 表 | 独立模块或插件仓库 | parent | `innospots-nexus-plugin` |

完整组装决策表见 dependency-conventions →「可运行应用组装决策表」。

---

## 产出位置（会改哪些文件）

### 新建内部库模块 `innospots-nexus-<name>`

| 文件 | 变更 |
|------|------|
| `innospots-nexus/pom.xml` | `<modules>` 注册 |
| `innospots-nexus-<name>/pom.xml` | 新建（parent、artifactId、最小 dependencies） |
| `innospots-nexus-bom/pom.xml` | 若被他模块依赖 → `dependencyManagement` 登记 `${revision}` |
| `innospots-nexus-<name>/src/main/java/...` | **仅包根** `com.innospots.nexus.<name>`（空目录或 package-info，无领域子树） |
| `innospots-nexus-<name>/src/test/java/...` | 镜像包根 |

**不在此阶段创建**：`role/`、`service/`、`endpoint/` 等领域子包（属 design/develop）。

### 新增第三方依赖

| 顺序 | 文件 |
|------|------|
| 1 | `innospots-nexus-bom/pom.xml` — `<properties>` + `dependencyManagement` |
| 2 | 消费模块 `pom.xml` — 只写 `groupId:artifactId`，**无 version** |
| 3 | 可选 parent — 通用注解处理器进 `annotationProcessorPaths` |

须先 `java:design` 选型评估（见 architecture-decision 触发项）。

### 新建 Spring / Quarkus 子模块

| 文件 | 变更 |
|------|------|
| `innospots-nexus-spring/pom.xml`（或 quarkus） | `<modules>` |
| 新子模块 `pom.xml` | parent 指向 spring/quarkus 聚合 |
| BOM | 新 artifact 登记 |
| starter 版本 | **只改 BOM** `spring-boot.version`（见 java:spring） |

中立库模块（base/core/console/kernel/platform）**不得**在此阶段引入 starter。

---

## 新建模块标准流程

```text
1. grill-me（结构变更类必经）→ 记录结论
2. 确认模块类型（上表）与依赖方向（module-ownership / module-layout）
3. 注册 <modules> + 编写模块 pom.xml（模板见 build-config.md）
4. BOM 登记（若可被依赖）
5. 创建 src/main/java、src/test/java 与包根 only
6. mvn validate
7. mvn -pl innospots-nexus-<name> -am clean compile
8. mvn -q help:effective-pom
9. mvn -pl innospots-nexus-<name> dependency:tree
10. mvn dependency:analyze（关注未声明/未使用）
11. （可选）mvn -pl innospots-nexus-<name> -am clean install
12. 交 java:design（领域包、契约）→ 再 java:develop
```

结构变更 PR **不应**夹带业务实现；POM-only 便于回归判定。

---

## 验证命令（工程交付最小集）

```bash
mvn validate
mvn -pl <module> -am clean compile
mvn -q help:effective-pom
mvn -pl <module> dependency:tree
mvn dependency:analyze
```

排错见 [build-config.md](build-config.md) →「常见构建问题定位」。

---

## 交下游前交付清单

- [ ] grill-me 结论已记录（结构变更类）
- [ ] 新模块类型与依赖方向符合 module-layout / AGENTS.md
- [ ] 无模块 POM 内联 `<version>`
- [ ] 新第三方已在 BOM 登记
- [ ] `validate` + `compile` 通过
- [ ] `dependency:tree` 符合最小依赖（无多余 console/plugin 重复声明）
- [ ] 未在中立库模块引入 Spring/Quarkus starter
- [ ] kernel ↔ platform 无互依
- [ ] 仅创建包根，未预建领域 `service`/`event` 空树
- [ ] 若模块职责变化，已评估是否更新 `AGENTS.md`（须单独说明）
- [ ] 下一步已明确：`java:design`（非直接 develop）

全量 L0–L5 由 `java:check` 在 develop 完成后执行。

---

## 禁止产出

| 禁止 | 应转交 |
|------|--------|
| 业务 Entity/Endpoint/Service 源码 | `java:develop` |
| 设计文档、API 契约正文 | `java:design` |
| 根 `${revision}` 升版 | `java:project-upgrade` |
| BOM 第三方大版本升级 | `java:dependency-upgrade` |
| 复制遗留多模块 POM 结构 | 只参考行为，按本仓库分层重建 |

---

## 专题索引

| 主题 | 文件 |
|------|------|
| 依赖引用与可运行应用 | [dependency-conventions.md](dependency-conventions.md) |
| parent/BOM/插件/POM 模板 | [build-config.md](build-config.md) |
| 模块职责与包结构 | [module-layout.md](module-layout.md) |
| 领域包（非 Maven 模块） | [package-structure.md](../../java-reference/references/package-structure.md) |
| 实现交付物 | [develop-deliverables.md](../../java-develop/references/develop-deliverables.md) |
| Spring 依赖边界 | [spring-dependencies.md](../../java-spring/references/spring-dependencies.md) |
