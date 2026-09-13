# Java 编码规范（权威原文）

本目录是 `java:reference` 技能包的规范条文**唯一权威来源**，位于
`skills/java/java-reference/standards/`（随 `skills/java/` 一起安装）。

消费入口：`java:reference`（`SKILL.md` 索引与 `references/` 专题参考）→ 本目录原文。

包结构（领域优先）的专题说明在 `../references/package-structure.md`（非本目录条文，但与
`naming.md`「包命名」、`domain-module-initialization.md` §1.4 配套）。

| 文件 | 管辖 |
|------|------|
| `code-style.md` | 格式、Lombok、REST、MapStruct、DAO、日志 |
| `naming.md` | 命名、词汇、包与持久化命名 |
| `api-design.md` | 签名、契约、实体、事务、事件、兼容性 |
| `code-comments.md` | 注释与 TODO |
| `exception-status-code.md` | 异常与状态码 |
| `domain-module-initialization.md` | 六阶段领域初始化 |
| `module-skills.md` | 模块 API 参考生成策略（`README.md` 索引，非技能） |

仓库级模块职责见根目录 `AGENTS.md`。
