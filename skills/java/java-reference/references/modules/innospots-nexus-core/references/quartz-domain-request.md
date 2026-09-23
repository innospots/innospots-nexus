# 包 `quartz.domain.request`

## QuartzJobRequest

**类型：** record

调度 Quartz 作业的请求；构造时校验必填字段。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `jobName` | `String` | 唯一作业名称（必填、非空白） |
| `jobClass` | `Class<? extends Job>` | Quartz Job 实现类（必填） |
| `scheduleMode` | `ScheduleMode` | 调度模式（必填） |
| `cronExpression` | `String` | Cron 表达式（CRON/SCHEDULED 模式必填） |
| `startTime` | `Date` | 首次触发时间（可选；ONCE 模式默认为当前时间） |
| `endTime` | `Date` | SCHEDULED 模式的结束时间 |
| `dataMap` | `Map<String, ?>` | 作业数据映射（防御性复制） |

### 构造方法

#### `QuartzJobRequest()`


### 方法

#### `cron(String jobName, Class<? extends Job> jobClass, String cronExpression, Map<String, ?> dataMap) → QuartzJobRequest`

- **说明：** 创建 Cron 触发作业请求。
- **参数：**
  - `jobName` — 作业名称
  - `jobClass` — 作业类
  - `cronExpression` — Cron 表达式
  - `dataMap` — 作业数据
- **返回：** 作业请求

#### `once(String jobName, Class<? extends Job> jobClass, Date startTime, Map<String, ?> dataMap) → QuartzJobRequest`

- **说明：** 创建在指定开始时间触发一次的作业请求。
- **参数：**
  - `jobName` — 作业名称
  - `jobClass` — 作业类
  - `startTime` — 开始时间
  - `dataMap` — 作业数据
- **返回：** 作业请求

#### `scheduled(String jobName, Class<? extends Job> jobClass, String cronExpression, Date endTime, Map<String, ?> dataMap) → QuartzJobRequest`

- **说明：** 创建带 Cron 表达式与可选结束时间的调度作业请求。
- **参数：**
  - `jobName` — 作业名称
  - `jobClass` — 作业类
  - `cronExpression` — Cron 表达式
  - `endTime` — 结束时间
  - `dataMap` — 作业数据
- **返回：** 作业请求
