# 包 `logger`

## AuditLogMessages

**类型：** class（final，静态工具）

将注解中的 `I18nLiteral` 转为 `I18nObject`，并按当前线程 locale 解析审计操作文案。

### 方法

| 方法 | 说明 |
|------|------|
| `toI18nObject(I18nLiteral) → I18nObject` | 校验偶数个 locale/文案对并构造多语言对象 |
| `resolveOperationMessage(I18nLiteral) → String` | 解析当前 locale 下的操作说明 |

## I18nLiteral

**类型：** annotation，`@Target({})`

注解内嵌的多语言文案声明。`pairs` 交替传入 BCP 47 语言标签与文案。

## InvocationLogHandler

**类型：** interface

框架无关调用日志处理器端口，接收已组装的 `InvocationLogContext`。

## LogExecutor

**类型：** class（final）

框架适配器共享的拦截例程：记录耗时与结果，并委托 `InvocationLogHandler`。
