# 包 `domain.identity`

身份相关的传输/会话快照。非内核领域实体。

## RoleSnapshot

**Type:** record

角色定义，含标识、显示名称及程序编码。

## UserGroupSnapshot

**Type:** record

用户组/团队，含层级（父组）、负责人及协助用户。

## UserSnapshot

**Type:** class

用户的会话/传输快照。不可变身份字段（`userId` 为 `Long`、
`userName`、`realName`）及可变属性（email、avatar、group、
roles、`lastAccessTime`、`BasicStatus`）。

| 工厂/辅助 | 说明 |
|------------------|-------------|
| `simple(userId, userName, realName)` | 最小快照 |
| `fromContextOptional()` / `fromContext()` | 从 `TLC` 身份键重建 |
| `fromClaims(Map)` | 从令牌/会话声明构建（`TLC.USER_ID` 等） |
