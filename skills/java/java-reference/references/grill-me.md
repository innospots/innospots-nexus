# grill-me 使用指南

`grill-me` 是**方案与决策压力测试**技能，通过多轮结构化问答（`/grilling` 会话）
在动手前消歧义、暴露隐含假设、固化设计树。它**不产出代码**，产出是双方确认的设计结论。

触发方式：在对话中附加 `grill-me` 技能，或使用 `/grill-me` + 待审查范围（模块路径、
方案文档、技能目录等）。

---

## 安装（未安装时必须先执行）

`java:project`（新建 Maven 模块）与 `java:design`（新设计工作）**开始前**必须先能调用
`grill-me`。若当前环境未安装该技能，Agent **不得**跳过或代行设计决策，须先安装并按技能全文执行：

```bash
npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"
```

安装后：

1. **立即**按生成技能中的完整说明执行（`Read its complete output`）。
2. 若终端输出过长，先**重定向到临时文件**再读取全文。
3. 技能若提供 **supporting-files** 目录，其中的**相对路径一律以该目录为根**解析
   （`Resolve relative paths from the supporting-files directory it provides`）。
4. grill-me 会话结束且开发者确认一致后，方可进入 `java:project` 或 `java:design`。

---

## 何时调用

在**重大决策尚未固化、存在多个可行分支**时调用。Agent 自行查证事实，把**决策**留给开发者。

| 阶段 | 调用时机 | 审查对象示例 |
|------|---------|-------------|
| **`java:project` 之前（必经）** | **新建 Maven 模块、调整 reactor、改依赖方向** | `pom.xml`、`module-layout.md`、目标模块路径 |
| **`java:design` 之前（必经）** | **新业务域、新设计、新增模块对应的设计** | `skills/java/`、目标模块、`docs/design/*.md` |
| 设计前 | 跨模块能力、边界仍模糊 | 同上 |
| 设计后 / 开发前 | 四步法草稿完成，准备交 `java:develop` | 归属表、词汇表、契约骨架 |
| 工程结构变更前 | 新建模块、改依赖方向、拆合模块 | `pom.xml`、`module-layout.md`、目标模块源码 |
| 升级方案前 | JDK / Spring / Jakarta / 模块迁移 | 升级范围、影响模块、回滚策略 |
| 架构评审触发项 | 见 `java:design` → `architecture-decision.md` | 对应方案或 ADR 草稿 |

## 何时不调用

| 场景 | 应使用的技能 |
|------|-------------|
| 查具体编码规范条文 | `java:reference` |
| 规范已清晰，直接写代码 | `java:develop` |
| 写测试、跑编译验证 | `java:develop` / `java:check` |
| 修已定位的局部 Bug | `java:develop`（先复现测试） |
| 执行已确认的升级步骤 | `java:project-upgrade` / `java:dependency-upgrade` |

**不得在实现中途用 grill-me 代替设计**；若开发中发现归属/契约错误，应回到
`java:design` 修订，必要时再开一轮 grill-me。

---

## 会话规则（摘要）

1. 将待决事项映射为**设计树**，按轮次提问「当前前沿」上的所有问题
2. 每题给出**推荐答案**；开发者逐轮回答后再进入下一轮
3. 事实由 Agent 查代码/文档，**不问开发者能查到的内容**
4. 前沿为空且开发者确认理解一致后，会话结束，**方可动手实施**

完整协议见仓库或个人技能目录中的 `grilling` / `grill-me` 技能定义。

---

## 与各 Java 技能的衔接

```text
grill-me          新建模块 / 新设计前必经（未安装则先安装）
    ↓
java:project      需要新建 Maven 模块时（按需）
    ↓
java:reference    查规范红线（贯穿）
    ↓
java:design       定归属、词汇、边界、契约（新设计必经）
    ↓
（可选）grill-me  四步法结论交 develop 前复审
    ↓
java:develop      测试先行（契约红灯）→ 六阶段实现 → mvn test
    ↓
java:check        验证出口
```

### 按技能

| 技能 | grill-me 角色 |
|------|----------------|
| `java:reference` | 规范问题不走 grill-me；**边界/归属/方案歧义**时建议先 grill |
| `java:design` | **开始前必经**；四步法完成后交 develop 前可再开一轮复审 |
| `java:project` | **新建模块、改 reactor、动依赖方向之前必经** |
| `java:develop` | 默认不调用；阶段零未通过时回到 design，必要时 grill；测试策略分歧时回到 design → `test-scope.md` |
| `java:check` | 不用于合入门禁；可选用于大变更前的方案复审 |
| `java:dependency-upgrade` | JDK / Spring / 大依赖迁移方案定稿**之前**；组件替换路径不唯一时 |
| `java:project-upgrade` | 发版策略、多版本线协调有歧义时（日常 revision bump 通常不需要） |
| `java:spring` | Spring 与 jakarta.ws.rs 边界冲突、集成方案未定时 |

---

## 推荐审查范围（示例）

```text
/grill-me innospots-nexus-kernel/src/.../permission
/grill-me skills/java
/grill-me docs/design/multi-tenant-governance-design.md
/grill-me JDK 25 + Spring Boot 4 升级方案
```

范围越具体，问题越聚焦；模块级或文档级审查优于空泛的「帮我设计 XX」。

---

## 出口门禁

grill-me 会话结束且开发者确认一致后：

- [ ] 关键决策已记录（可写入 ADR 或 design 结论）
- [ ] 无未闭合的归属/词汇/兼容面假设
- [ ] 明确下一步走哪个 `java:*` 技能
- [ ] **未在确认前开始写实现代码**
