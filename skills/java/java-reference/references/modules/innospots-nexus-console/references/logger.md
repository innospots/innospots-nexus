# 包 `logger`

## AuditLogMessages

**类型：** class（final，静态工具）

将 core 包 `endpoint.grant.I18nLiteral` 转为 `I18nObject`，并按当前线程 locale 解析审计操作文案（委托 `I18nLiteralSupport`）。

### 方法

| 方法 | 说明 |
|------|------|
| `toI18nObject(I18nLiteral) → I18nObject` | 校验偶数个 locale/文案对并构造多语言对象 |
| `resolveOperationMessage(I18nLiteral) → String` | 解析当前 locale 下的操作说明 |

## I18nLiteral

已迁至 **core** → `com.innospots.nexus.core.endpoint.grant.I18nLiteral`；解析见
`I18nLiteralSupport` 与本类静态方法。

## InvocationLogHandler

**类型：** interface

框架无关调用日志处理器端口，接收已组装的 `InvocationLogContext`。

## LogExecutor

**类型：** class（final）

框架适配器共享的拦截例程：记录耗时与结果，并委托 `InvocationLogHandler`。
