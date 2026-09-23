# 包 `quartz.service`

## QuartzScheduleManager

**类型：** class

围绕 Quartz {@link Scheduler} 的封装，在单一分组内管理作业与触发器，使用内存（RAM）作业存储。 {@link ScheduleMode#SCHEDULED} 与 {@link ScheduleMode#MANUAL}（拒绝调度）。</p>

### 方法

#### `startup() → void`

- **说明：** 延迟初始化并启动 Quartz 调度器（双重检查锁定）。 幂等，可安全多次调用。

#### `refreshJob(QuartzJobRequest request) → boolean`

- **说明：** 调度或更新作业。作业已存在时替换并重新调度触发器（幂等）。
- **参数：**
  - `request` — 作业请求
- **返回：** 接受作业时返回 {@code true}；模式为 MANUAL 或 Cron 无效时返回 {@code false}

#### `deleteJob(String jobName) → boolean`

- **说明：** 删除作业及其关联触发器。
- **参数：**
  - `jobName` — 作业名称
- **返回：** 作业不存在时返回 {@code false}

#### `pauseJob(String jobName) → boolean`

- **说明：** 暂停作业。
- **参数：**
  - `jobName` — 作业名称
- **返回：** 作业不存在时返回 {@code false}

#### `resumeJob(String jobName) → boolean`

- **说明：** 恢复已暂停的作业。
- **参数：**
  - `jobName` — 作业名称
- **返回：** 作业不存在时返回 {@code false}

#### `hasJob(String jobName) → boolean`

- **说明：** 检查作业是否存在。
- **参数：**
  - `jobName` — 作业名称
- **返回：** 存在时返回 {@code true}

#### `scheduleJobs() → Set<String>`

- **说明：** 返回配置分组内全部作业名称。
- **返回：** 作业名称集合

#### `schedulerInfo() → List<QuartzJobInfo>`

- **说明：** 返回配置分组内全部作业的详细信息。
- **返回：** 作业信息列表

#### `latestUpdateTime() → LocalDateTime`

- **说明：** 返回最后一次作业调度变更的时间戳。
- **返回：** 最近更新时间

#### `shutdown() → void`

- **说明：** 停止调度器、清空全部作业并释放资源。 可安全多次调用。
