# 包 `quartz.converter`

## CronConverter

**类型：** class

将常见调度时间参数转换为 Quartz Cron 表达式。 Quartz Cron 使用六个必填字段： second minute hour day-of-month month day-of-week。本转换器显式保持映射关系， 调用方可提供领域级时间参数而无需手工拼接 Cron 字符串。

### 方法

#### `convert(TimePeriod timePeriod, List<String> periodTimes, LocalTime localTime) → String`
- **说明：** 将通用周期与周期值转换为 Quartz Cron 表达式。 取值规则： TimePeriod 期望一个分钟间隔； TimePeriod 接受一个间隔或多个选定小时； TimePeriod 接受可选的天间隔； TimePeriod 接受星期值，如 1、MON 或 MONDAY； TimePeriod 接受 1 至 31 的月中日期。
- **参数：**
  - `timePeriod` — 周期类型
  - `periodTimes` — 周期取值列表
  - `localTime` — 本地时刻
- **返回：** Quartz Cron 表达式

#### `everyMinutes(int minutes) → String`
- **说明：** 构建每 minutes 分钟触发一次的 Cron 表达式。
- **参数：**
  - `minutes` — 分钟间隔
- **返回：** Cron 表达式

#### `everyHours(int hours, LocalTime localTime) → String`
- **说明：** 构建每 hours 小时触发一次的 Cron 表达式。 localTime 为 null 时，分秒字段默认为午夜。
- **参数：**
  - `hours` — 小时间隔
  - `localTime` — 本地时刻
- **返回：** Cron 表达式

#### `hourlyAt(Collection<Integer> hours, LocalTime localTime) → String`
- **说明：** 构建在每天选定小时触发的 Cron 表达式。 分秒字段取自 localTime；为 null 时使用午夜。
- **参数：**
  - `hours` — 小时集合
  - `localTime` — 本地时刻
- **返回：** Cron 表达式

#### `daily(int days, LocalTime localTime) → String`
- **说明：** 构建每 days 天在 localTime 触发一次的 Cron 表达式。
- **参数：**
  - `days` — 天间隔
  - `localTime` — 本地时刻
- **返回：** Cron 表达式

#### `weekly(Collection<DayOfWeek> daysOfWeek, LocalTime localTime) → String`
- **说明：** 构建在选定星期几于 localTime 触发的 Cron 表达式。
- **参数：**
  - `daysOfWeek` — 星期集合
  - `localTime` — 本地时刻
- **返回：** Cron 表达式

#### `monthly(Collection<Integer> daysOfMonth, LocalTime localTime) → String`
- **说明：** 构建在选定月中日期于 localTime 触发的 Cron 表达式。
- **参数：**
  - `daysOfMonth` — 月中日期集合
  - `localTime` — 本地时刻
- **返回：** Cron 表达式
