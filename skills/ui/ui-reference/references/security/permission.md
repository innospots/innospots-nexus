# Permission 模型

## 三种 YAML 形态

### 1. 单权限码（字符串）

```yaml
permission: customer:view
```

### 2. 任一满足（数组）

```yaml
permission:
  - customer:edit
  - customer:admin
```

### 3. 详细对象

```yaml
permission:
  code: customer:delete
  denied: disabled   # hidden | disabled | readonly，默认 hidden
```

## 可声明位置

| 位置 | 说明 |
|------|------|
| `page.permission` | 页面级访问控制 |
| 组件节点 `permission` | 节点可见/可用 |
| `ActionConfig.permission` | 动作执行权限 |

## denied 行为

| 值 | 含义（运行时） |
|----|----------------|
| `hidden` | 隐藏 |
| `disabled` | 禁用 |
| `readonly` | 只读 |

## Java

- 类型：`PermissionConfig`
- 反序列化：`PermissionConfigDeserializer`
- 工厂：`PermissionConfig.code()` / `anyOf()` / `detailed()`

## 注意

Validator **不**检查权限码是否在 IAM 注册；仅校验 YAML 形状。
