# 权限声明（`permission`）

可在 **页面**（`page.permission`）、**动作**（`ActionConfig.permission`）、**组件**（`ComponentNode.permission`）声明。

## YAML 形态

### 单个代码（字符串）

```yaml
page:
  permission: customer:view
```

### 任一匹配（数组）

```yaml
permission:
  - customer:edit
  - customer:admin
```

### 详细对象

```yaml
permission:
  code: customer:delete
  denied: disabled
```

动作示例：

```yaml
- action: confirm
  permission: customer:delete
```

组件示例：

```yaml
- type: Button
  permission:
    code: customer:delete
    denied: disabled
```

## `denied` 行为

| 值 | 说明 |
|----|------|
| `hidden` | 默认；无权限时不渲染 |
| `disabled` | 无权限时禁用展示 |

对应 Java：`PermissionDenied` 枚举。

## 与控制台授权的关系

- 权限 **代码字符串** 与 `nx_permission_grant`、目录资源码一致（由 console 权限运行时解析）。
- Page DSL 只**声明**所需权限；**不**存储授权关系。
- 页面能否打开还受 catalog 中 PAGE 资源与 `ConsolePagePermissionAuthorizer` 约束。

## 校验

`PageDslValidator` 校验 `permission` 对象可反序列化为 `PermissionConfig`；不校验代码是否在租户中已配置。
