# Service registry（`core.server`）

平台级服务节点注册与本地运行时持有者。

## ServiceRegistry

**类型：** interface

| 方法 | 说明 |
|--------|-------------|
| `register(ServiceInfo)` | 插入或更新节点 |
| `unregister(serverKey)` | 按 `host:port` 键移除 |
| `findByKey(serverKey)` | 查找 |
| `listOnline()` | 仅 `ONLINE` 状态 |
| `listAll()` | 所有已注册节点 |

实现可通过宿主/适配器层的 `ServiceRegistryEntity` 持久化。

## ServiceInfo

**类型：** class（流式构建器风格）

- 标识：`serviceName`、`instanceId`
- 网络：`host`、`port` → `serverKey()` = `host:port`
- `ServiceStatus`、`ServiceRole`、`group`、标签、指标映射
- `updatedAt` 心跳时间戳

## ServiceNodeHolder

**类型：** class

用于集群协调的本地节点状态：

- 持有当前 `ServiceInfo`、启动时间、Leader 标志
- `maxValidSeconds` 用于心跳过期判断
- 设置 `availableServicesSize` 时的分片位置辅助方法

## ServiceRegistryEntity

**类型：** class，继承 `BaseEntity`

表：`nx_service_registry`。平台作用域（无租户/工作空间列）。

## ServiceRole / ServiceStatus

**类型：** enum

已注册实例的集群角色与可达性。

## ServiceLifecycle

**类型：** class

跟踪运行中节点进程的本地生命周期转换。
