# 包 `server.runtime`

## ServiceNodeHolder

**类型：** class

持有本地节点的服务注册状态，并提供 Leader 判定、心跳失效检测与分片计算。 所有状态字段为 volatile，保证跨线程可见性。

### 方法

#### `lifecycle() → ServiceLifecycle`
- **说明：** /
- **参数：**
  - `maxValidSeconds` — 自上次心跳起超过该秒数则视为节点无效

#### `markStartup() → void`
- **说明：** 将当前时间记录为节点启动时刻。 public void markStartup()

#### `startupTime() → LocalDateTime`
- **说明：** 返回已记录的启动时间；尚未标记时返回 null。
- **返回：** 启动时间

#### `register(ServiceInfo service) → void`
- **说明：** 注册或更新当前节点的服务信息；Leader 状态由服务角色推导。
- **参数：**
  - `service` — 服务信息

#### `unregister() → void`
- **说明：** 注销当前节点并重置 Leader 状态。 public void unregister()

#### `isRegistered() → boolean`
- **说明：** 当前节点已注册服务时返回 true。 public boolean isRegistered()

#### `currentService() → ServiceInfo`
- **说明：** 返回当前服务信息；未注册时返回 null。
- **返回：** 当前服务信息

#### `isLeader() → boolean`
- **说明：** 当前节点为 Leader 且心跳仍在有效窗口内。
- **返回：** 是否为活跃 Leader

#### `isInvalid(ServiceInfo service) → boolean`
- **说明：** 远程服务心跳超过 maxValidSeconds 时视为无效。
- **参数：**
  - `service` — 待检查的服务
- **返回：** 是否无效

#### `position() → int`
- **说明：** 返回节点在集群中的位置（从 0 起）；未设置时返回 -1。
- **返回：** 节点位置

#### `availableServicesSize() → int`
- **说明：** 返回集群中可用服务总数。
- **返回：** 可用服务数

#### `computeShardingKeys(int totalKeys) → int[]`
- **说明：** 根据节点在集群中的位置，用向上取整除法计算分配给本节点的分片键范围。
- **参数：**
  - `totalKeys` — 分片键总数
- **返回：** 键索引数组；位置未设置或总数为 0 时返回空数组
