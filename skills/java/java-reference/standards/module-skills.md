# 模块 API 参考

开发者触发的 Java 模块公共 API 快照。这些是供 `java:reference` 消费的**参考索引**——**不是**可安装的 Cursor 技能。

## 生成策略

- 在实现功能、修复缺陷、重构或更改包时，不要创建、编辑或同步模块 `README.md` 或 `references/` 下的文件。
- 添加、删除、重命名或移动 Java 类型不得自动触发模块 API 文档变更。
- 仅当开发者明确请求扫描并标识模块或项目范围时，才生成或刷新模块 API 文档。
- 请求的扫描必须检查所选完整源目录，并一致地重新生成对应的 `README.md` 和 `references/` 集。
- 不要从代码任务、文档任务、构建失败或过时参考内容推断文档请求。
- 在开发者请求的扫描之间，现有模块 API 文档可能暂时过时。

当明确请求扫描时，每个选定的 Java 模块文档位于：

```text
skills/java/java-reference/references/modules/<artifact-id>/
├── README.md          模块 API 索引（入口 — 不是 SKILL.md）
└── references/        按包的详细文件
```

`java:reference` 技能索引这些模块参考。不要将 `SKILL.md` 或技能式 YAML front matter（`name:`、`description:`）放在 `references/modules/` 下——该路径仅保留给 `java:*` 可执行技能。

## README.md 格式

纯 markdown 索引。**无 YAML front matter。**

### 必需章节（按顺序）

| 章节 | 内容 |
|---------|---------|
| `# <artifact-id> — 模块 API 索引` | 标题 + 说明这不是 Cursor 技能 |
| `## 模块概览` | 简要介绍 +「它能做什么」能力表 |
| `## 类参考` | 按包的表：`\| 类 \| 类型 \| 说明 \|` |
| `## 包参考` | 映射到 `references/` 中各包文件的表 |

可选快照元数据（纯文本，非 YAML）：

```markdown
快照版本： 1.2.0
```

### 能力表示例

```markdown
## 模块概览

**能力一览：**

| 能力 | 说明 |
|------------|-------------|
| **条件引擎** | 用类型化因子与运算符构建过滤/规则条件 |
| **身份与访问** | 建模用户、角色、组与组织 |
| ... | ... |
```

### 类参考示例

```markdown
### 包 `domain.condition`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `Factor` | `class` | 单条过滤条件，支持占位符解析 |
| `Operator` | `enum` | 比较运算符，含 DB/脚本双符号 |
```

## references 目录

模块中每个包在 `references/` 中有对应的 markdown 文件，命名为 `<package-name>.md`（点替换为连字符，例如 `domain.condition` 对应 `domain-condition.md`）。文件文档化包中每个 public class/enum/interface/record/annotation：

```markdown
# 包名

## ClassName

**类型：** class/enum/interface/record/annotation

类用途说明。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `fieldName` | `FieldType` | 组件含义与约束 |

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `fieldName` | `FieldType` | 字段含义与约束 |

### 枚举常量

| 常量 | 说明 |
|------|------|
| `CONSTANT` | 常量含义 |

### 构造方法

#### `ClassName(ParamType param)`
- **说明：** 构造器职责与校验行为
- **参数：**
  - `param` — 参数说明
- **异常：**（如适用）

### 方法

#### `methodName(ParamType param) → ReturnType`
- **说明：** 方法做什么
- **参数：**
  - `param` — 参数说明
- **返回：** 返回值说明
- **异常：**（如适用）
```

生成要求：

- **record** 必须列出全部组件（名称、类型、说明）；说明优先取自类型 Javadoc 的 `@param`，其次取自工厂方法/紧凑构造器。
- **class** 必须列出带 Javadoc 的**成员变量**（名称、类型、说明）。
- **class / record** 必须列出全部 **public/protected 构造方法**（签名、说明、参数、异常）。
- **enum** 列出各枚举常量说明（业务含义不显而易见时必填）。

## 一致性规则

- 一致性要求在一次明确请求的扫描结果内成立，而非每次源码修改之后。
- 完整模块扫描应文档化范围内每个 public API 包。
- 生成的 `README.md` 类参考及其 `references/` 文件必须描述同一扫描源快照。
- 不要手动修补单个参考条目以跟随孤立的代码变更；应重新运行开发者请求的目录扫描。
