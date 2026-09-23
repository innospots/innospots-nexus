# 包 `quartz.domain.model`

## QuartzJobInfo

**类型：** record

已调度 Quartz 作业及其触发器的不可变快照。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `jobName` | `String` | 作业名称 |
| `jobGroup` | `String` | 作业分组 |
| `jobClass` | `String` | 作业实现类的全限定名 |
| `dataMap` | `Map<String, Object>` | 作业数据映射（防御性复制） |
| `triggers` | `List<QuartzTriggerInfo>` | 关联触发器信息（防御性复制） |

### 构造方法

#### `QuartzJobInfo()`

## QuartzTriggerInfo

**类型：** record

Quartz 触发器当前状态的不可变快照。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `triggerName` | `String` | 触发器名称 |
| `triggerGroup` | `String` | 触发器分组 |
| `startTime` | `Date` | 首次计划触发时间 |
| `nextFireTime` | `Date` | 下次预期触发时间（未调度时为 {@code null}） |
| `endTime` | `Date` | 结束时间（无结束时为 {@code null}） |
| `finalFireTime` | `Date` | 最后一次触发时间（尚未触发时为 {@code null}） |
| `calendarName` | `String` | 日历名称（无日历时为 {@code null}） |
| `mayFireAgain` | `boolean` | 触发器是否可能再次触发 |
