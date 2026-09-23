# 异常与状态码规范设计

**日期：** 2026-08-28

**范围：** 为异常与状态码设计建立单一事实来源，
并在 API 与 domain initialization 规范中添加聚焦交叉引用。
本任务不修改任何 Java 源码名称或实现。

## 目标

使 domain、infrastructure、REST、plugin 与
extension 边界上的失败可预测。读者应能确定抛哪种异常、
选哪种或新增哪种状态码、如何保留原始 cause、
如何向调用方暴露失败，以及如何扩展码系而不破坏模块归属或兼容性。

## 事实来源

规则源自当前实现：

- `NexusException` 是共享 runtime 异常，携带 machine-readable
  code、summary message、可选本地化 display 与可选 cause。
- `StatusCode` 定义 module、category、四位 local code、
  双语 message/advice 与 HTTP status 元数据。
- `NexusStatusCode` 拥有平台级 `AIO` 码。
- `PluginStatusCode` 作为 technical module 拥有 plugin-runtime `PLG` 码。
- `StatusCodeRules` 校验 module/category/local-code 形状。
- `R<T>` 将 `NexusException` 转换为 transport failure response。

文档将区分当前实现行为与推荐用法。不会默许超出本规范任务范围的源码变更。

## 目标文档

### `standards/exception-status-code.md`

创建权威规范，包含以下章节：

1. failure taxonomy 与 exception ownership；
2. `NexusException` 构造、cause 保留、display message 与
   sensitive-data 规则；
3. catch/rethrow/wrap 与 boundary translation 规则；
4. status-code 结构、category 选择、HTTP mapping 与命名；
5. 平台级 status-code 规则；
6. domain 与 technical module 扩展规则；
7. module-code 与 local-code 分配流程；
8. 兼容性、i18n、serialization 与 response mapping；
9. contract-test 要求与 review checklist。

### `standards/api-design.md`

保持 API 级异常与 status 指导简洁，链接到新事实来源，
并澄清现有规则：application/business failure 使用
`NexusException`；纯 utility programmer-precondition failure 仅在
不跨越 application boundary 时可使用 framework 合适的异常。

### `standards/domain-module-initialization.md`

在 domain contract 与 verification gate 中添加 status-code 与 exception 检查。
工作流要求在引入新 domain failure 前完成 reuse 检查、domain ownership、
stable code allocation、双语 message/advice 与 status-code contract test。

## 异常模型

规范定义四类实用 failure class：

- 预期 caller 或 business failure，由 `NexusException` 与
  可复用 `StatusCode` 表示；
- 转换后的 infrastructure/external failure，由
  带相关 status 并保留 cause 的 `NexusException` 表示；
- 纯 lower-level utility 的 programmer misuse，若从不代表
  application boundary 上的 user 或 business input，可保留
  framework/JDK precondition exception；
- cancellation/interruption 与 fatal JVM error，不得被吞掉
  或误标为普通 business failure。

不为每个 status code 创建 exception 子类。优先使用 `NexusException.build(...)`
overload；raw string 仍是显式 interop/extension boundary，必须校验并 allowlist。

## 状态码模型

文档化 canonical 格式为：

```text
MODULE(3 uppercase letters) + CATEGORY(2 digits) + LOCAL(4 digits)
```

例如 `AIO080002` 是 `AIO` module 中的 configuration error code。
full code 为九字符。category 表达 failure 语义；
HTTP status 表达 transport 行为，不替代 business code。

平台级码归属 `NexusStatusCode`。domain-specific business
code 归属 owning domain 的 `domain.enums` 包并实现
`StatusCode`。可复用 technical module 可在该 module technical boundary 附近
保留 module-local status enum，如 `core.plugin.status.PluginStatusCode`。
sibling business module 不得仅为共享 error 而 import 彼此的 status enum。

## 扩展流程

新增 status code 前：

1. 搜索同义现有 code，当 scope 与 remediation 兼容时复用；
2. 判断 failure 是 platform-wide、domain-specific 还是 technical；
3. 从 owning boundary 选择 module code 与 category；
4. 在该 module status family 内分配唯一四位 local code；
5. 提供稳定英文与中文 message/advice 文本，不含 runtime
   value 或 secret；
6. 映射到最窄的正确 HTTP status；
7. 添加 format、uniqueness、metadata 与 behavior contract test；
8. 在同一兼容性变更中更新 consumer 与文档。

扩展不得引入新 exception 子类、在不同 enum 下 duplicate 现有 status、
复用 code 表达不同含义，或随意变更现有 full code/message/event contract。

## 兼容性与验证

full code、enum constant name、HTTP mapping、event/configuration identifier
与 localized default message 均为 compatibility surface。重命名或
re-numbering 需要显式 migration 或 version boundary。现有
source-level 偏差可在单独实现变更中修正；
本任务仅改文档。

验证包括 Markdown 一致性检查、对照源码实现的聚焦 review，
以及仓库常规 Maven validation。不触发 module skill scan。

## 完成标准

- Exception 选择与 wrap 规则无歧义。
- `NexusException`、raw-code interop、cause 保留与 response mapping
  文档一致。
- Status-code format、category 语义、module ownership、local allocation、
  message、advice 与 HTTP mapping 明确。
- Domain 与 technical extension 有独立、合法的 placement 规则。
- 扩展前可见 compatibility 与 contract-test 要求。
- 仅变更预期的 standards/design/plan 文档；Java 源码与
  `standards/module-skills.md` 保持不变。
