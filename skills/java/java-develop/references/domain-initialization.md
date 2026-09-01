# 六阶段领域初始化

阶段门控：每阶段必须通过门禁才能进入下一阶段。完整权威定义在
`standards/domain-module-initialization.md`，本文件是执行清单。

---

## 阶段一：定义实体数据结构

实体设计放在第一位，因为它确立了领域身份、归属、持久化范围、唯一性、
关系、生命周期，以及后续契约所需的数据。

### 1.1 概念清单（先写下来再写 Java）

- 聚合/主记录
- 关联记录
- 父子关系
- 稳定业务键
- 生命周期与可见性字段
- 排序字段
- 受保护/内建标志
- 从共享基类继承的审计与租户/工作区字段

不得因为遗留工程把数据存在一起就合并相邻域。

### 1.2 选择持久化范围

| 范围 | 基类 |
|------|------|
| workspace（默认） | `WorkspaceBaseEntity` |
| tenant（不含工作区） | `TenantBaseEntity` |
| 平台级/全局 | `BaseEntity` |

禁止重新声明继承字段：`tenantId`、`workspaceId`、`createdAt`、`updatedAt`、
`createdBy`、`updatedBy`。

### 1.3 主键与稳定键

主键：`String` + `@TableId(type = IdType.ASSIGN_UUID)` + `@Id` +
`@Column(length = 32, nullable = false)`。

稳定键（如 `roleCode`、`menuKey`）：归属范围内唯一、创建后不可变、
变更会破坏引用时排除出 update 请求。

覆写 `BaseEntity.idPrefix()`，短小稳定。主键由 `DbPrimaryGenerator` 经
`IdGenerator.ulid(prefix)` 统一生成，operator **不得手工赋值**。

### 1.4 逐字段评审

每个字段确认：业务含义 / 必填选填 / Java 类型 / 持久化长度 / 默认值归属 /
是否可变 / 是否进索引 / 是否属于本域。

字符串长度用 2 的幂；无界文本用 `@Lob`；枚举值按 String 存储（除非项目另有明确映射）。

### 1.5 按访问模式定索引

| 访问模式 | 索引 |
|---------|------|
| 稳定唯一查找 | `uk_` |
| 外键/父级查找 | `idx_` |
| 常用状态与分页过滤 | `idx_` |
| 关联唯一性 | `uk_` |
| 树查询的兄弟排序 | `idx_` |

索引名显式且带表前缀。工作区唯一性含 `workspace_id`，租户唯一性含 `tenant_id`。

### 1.6 实体门禁（全部为“是”才继续）

- [ ] 实体在正确的模块与域
- [ ] 平台 / 租户 / 工作区范围正确
- [ ] 技术主键与稳定业务键已清晰分离
- [ ] 必填字段齐备
- [ ] 已移除过时或属于相邻域的字段
- [ ] 可空性与字符串长度是刻意为之
- [ ] 索引与预期查询路径匹配
- [ ] 使用 Lombok `@Getter` 与 `@Setter`
- [ ] JPA 与 MyBatis-Plus 注解都完整

**实现前先加实体契约测试**，验证表、继承、标识注解、必填字段、长度、可空性、索引，
并确认它因缺少契约而红灯。

---

## 阶段二：定义 DAO 契约

在实体契约稳定之后创建。

### 2.1 DAO 规则

- 放 `<domain>.dao`，`*Dao` 后缀，`extends BaseMapper<EntityType>`
- 优先使用继承的 CRUD
- 不创建 Mapper XML
- 不在 DAO 放业务工作流或事务编排
- **每个方法只访问一张表**
- 不得在 wrapper、注解 SQL、XML 或手写语句中使用 join

### 2.2 何时加自定义方法

仅当某个已知访问模式无法用继承操作清晰表达时。优先：

- `default` 方法 + MyBatis-Plus lambda wrapper（可复用谓词）
- 注解 SQL（单表查询写成 SQL 更清晰时）

初始化阶段**不得**创造投机性查询。

### 2.3 动词

DAO 方法可用 `select`/`insert`/`update`/`delete` 对齐 `BaseMapper`
（如 `selectByRoleCode`）。面向应用的 operator/service 用
`find`/`list`/`page`/`count`。DAO 方法可返回可空实体，在 operator/service
边界归一化缺失。

### 2.4 跨表读取五步

```text
1. 查询拥有方或关联表
2. 收集所有需要的标识或稳定键
3. 对每张相关表独立分批查询
4. 在内存中映射记录
5. 返回组装后的领域视图
```

N+1 一律禁止。

跨表写入、级联清理、关系完整性、稳定键重命名必须由带事务的 service 协调。
例如关联表存了 `tagName`，重命名标签定义必须在一个事务里通过两次独立 DAO 操作
更新两张表。

### 2.5 DAO 门禁

- [ ] 每个 DAO 只映射一个持久化实体
- [ ] 有独立实体的关联也有独立 DAO
- [ ] 没有 DAO 依赖 service 或 operator
- [ ] 自定义方法只描述直接的数据库操作
- [ ] 每个方法都可证明是单表
- [ ] 结果组装用分批查询而非 N+1
- [ ] 契约测试确认 `BaseMapper` 的泛型实体

---

## 阶段三：定义领域契约

### 3.1 枚举

放 `domain.enums`。表示封闭业务概念而非传输细节；语义匹配时复用平台枚举
（如 `BasicStatus`）；不合并其他域拥有的分类。

### 3.2 请求记录

放 `domain.request`，Java record，`Request` 后缀。

不同修改权必须拆分成不同请求：

`XxxCreateRequest` / `XxxUpdateRequest` / `XxxStatusUpdateRequest` /
`XxxPageRequest` / `XxxTreeRequest` / `XxxOrderRequest` /
`XxxAddRequest` / `XxxReplaceRequest`

- 不得把实体当请求用
- 创建请求可含稳定业务键；更新请求排除不可变键与受保护系统字段
- 查询类请求：显式 `@QueryParam`、必要时 `@DefaultValue`、bean 绑定需要时提供无参构造器、
  非法分页值归一化到共享默认值
- 集合组件：`null` 转不可变空集合、用 `List.copyOf`/`Set.copyOf`/`Map.copyOf`、
  绝不暴露调用方可变集合

### 3.3 视图记录

放 `domain.vo`，Java record，`Vo` 后缀。

按用例分离：详情/树视图、紧凑选择器选项、成员或关联视图、运行时/导航视图。

- 不得直接暴露持久化实体
- VO 可含：领域枚举（而非持久化的 String）、派生计数、树形子视图、
  审计时间戳、为只读运行时边界裁剪的精简字段
- 紧凑构造器中防御拷贝子对象与集合字段
- 主视图 `XxxVo`，特定投影把用例限定词放在 `Vo` 之前（`RoleOptionVo`）
- 禁用 `DTO`、大写 `VO`、`Response`

### 3.4 模型、事件与状态码

- `domain.model`：仅在内部业务需要既非实体也非传输 record 的模型时创建
- 领域事件：仅在存在真实的解耦交互时创建，发布域拥有事件契约
- 领域状态码：仅在现有平台状态码无法准确表达时创建，遵循
  `standards/exception-status-code.md` 的归属、类别、双语元数据、HTTP 映射与扩展安全规则

### 3.5 领域门禁

- [ ] 请求与 VO 是 record
- [ ] 名字反映用例
- [ ] 必要时创建与更新的修改权不同
- [ ] 集合不可变
- [ ] 仅用于持久化的 String 在适当位置以领域枚举暴露
- [ ] 没有越界的权限、用户、角色、菜单等相邻关注点
- [ ] 没有创建投机性的 model 或 event 包

对每个应用可见的失败再确认：

- [ ] 提议新码前已搜索现有状态码
- [ ] 平台/领域/技术归属明确
- [ ] 模块段与四位本地码唯一且稳定
- [ ] 类别、HTTP 映射、英文消息、中文建议都是刻意为之
- [ ] 用类型化 `StatusCode` 构造；翻译基础设施失败时保留 cause；未创建按错误划分的异常子类
- [ ] raw-code interop 不存在，或已通过显式 allowlist 校验

---

## 阶段四：定义转换边界

在 request / model / entity / VO 之间需要结构化映射时创建 MapStruct 转换器。

- 放 `<domain>.converter`，`*Converter` 后缀
- `@Mapper(config = BaseMapperConfig.class)`
- 转换 model 与 entity 时继承 `BaseBeanConverter<ModelType, EntityType>`
- 不得仅为拷贝一两个标量值而创建转换器
- 不得在端点、service、operator 里写大段逐字段拷贝

方法名在两个表示形态不明显时写明双方，如 `requestToModel`、`modelToEntity`。

方法有意未实现且暂无转换发生时，converter 包可以推迟。

---

## 阶段五：定义端点边界

端点定义传输边界，不拥有持久化与业务工作流。

### 5.1 按内聚资源边界拆分

出现以下差异即拆分：资源归属、路径层级、授权上下文、读写特征、消费者、
服务依赖、预期速率或生命周期。

不得创建一个堆积某业务名词下所有操作的端点。

### 5.2 初始形态（行为推迟时）

领域初始化期间创建**具体类**，除非开发者显式要求传输契约接口。

行为推迟时必须：

- 声明完整的 Jakarta REST 注解与方法签名
- 返回 `R<T>` / `R<PageResult<T>>` / `R<Void>`
- 加聚焦的 `TODO` 描述未来的 service 边界
- 抛带具体信息的 `UnsupportedOperationException`
- **不得**返回伪造的成功响应或空数据

### 5.3 方法规则

- 只用 Jakarta REST 注解
- 资源路径在类级，操作路径在方法级
- 路径、查询、头、bean 绑定参数全部显式注解
- 校验、事务、持久化、编排都不在端点
- 有结构化输入时用请求 record
- 名词与 HTTP 语义一致
- 单资源操作把稳定标识放路径
- 优先用专用排序请求而非重复标量参数
- 方法名与 service 边界用同一套命令/查询词汇；不得用 `process`/`handle`/`execute`

### 5.4 端点规模复审信号

- 同时管理生命周期与成员/分配
- 管理端写入与当前用户运行时读取混在一起
- 不同方法会依赖不同的未来 service
- 类级路径已无法自然描述所有方法
- 操作级授权会根本不同
- 再加一个方法就需要含糊命名来避免冲突

方法数不是唯一标准，但**超过约 7 个内聚操作应触发边界复审**。

### 5.5 端点门禁

- [ ] 端点边界内聚
- [ ] 必要时管理与运行时关注点已拆分
- [ ] 每个方法返回共享的 `R` 包装
- [ ] 请求与响应类型是领域 record
- [ ] 所有参数显式注解
- [ ] 推迟的方法显式失败
- [ ] 没有端点直接依赖 DAO
- [ ] 没有把权限资源操作塞进别的域

加基于反射的端点契约测试，覆盖类形态、路径、HTTP 注解、方法签名、
请求/VO record 类型与显式推迟行为。

---

## 阶段六：按需添加 Operator 与 Service

**不要自动脚手架化 operator 与 service。**

### 何时创建 Operator

实现主要面向一个 DAO 的直接数据操作时。仅在操作保持简单内聚时才可跨多 DAO。

### 何时创建 Service

- 非平凡工作流或校验
- 跨多次写入的事务
- 跨多个 operator 的协调
- 跨领域行为
- 树级联、受保护记录、生命周期编排
- 授权感知的组装

### 依赖方向

```text
endpoint → service → operator → dao
```

允许简化：

```text
endpoint → operator → dao
service → dao
```

operator 不得依赖另一个 operator 或 service；端点不得直接编排 DAO。

---

## 强制编译门禁

每批 Java 源文件改动之后立即运行：

```bash
mvn clean compile
```

不得延后到任务末尾，不得下调配置的 Java release 以匹配更旧的本地 JDK。

编译失败时：

1. 判断失败是否由本次新域改动引起
2. 修正导入、签名、注解、泛型或模块依赖
3. 重跑 `mvn clean compile`
4. 源码不可编译时不得继续添加功能

---

## 完整验证门禁

聚焦测试与编译通过后：

```bash
mvn validate
mvn test
mvn -q help:effective-pom
git diff --check
```

并巡检：

- `git status --short` 是否有意外文件
- 领域目录树是否有空的或不必要的包
- 导入是否有被禁止的框架耦合
- 实体字段是否有缺失或无关数据
- 端点路径与方法数是否有边界漂移
- 状态码归属、全码唯一性、HTTP 映射、异常翻译是否符合
  `standards/exception-status-code.md`
- diff 中是否有复制的遗留代码或机械复刻的包结构

本地 JDK 旧于配置 release 时**报告不匹配**，而不是改动工程基线。

---

## 完成检查清单

### 边界

- [ ] 归属 Maven 模块正确
- [ ] 领域未与 base / core / console 职责重复
- [ ] 遗留代码只用于理解行为
- [ ] 相邻域保持分离

### 实体

- [ ] 持久化范围与基类正确
- [ ] 主键注解与长度完整
- [ ] 稳定业务键与变更规则明确
- [ ] 必填字段、可空性、长度已评审
- [ ] 索引匹配唯一性与查询路径
- [ ] 未重复继承字段

### DAO 与领域

- [ ] DAO 继承正确的 `BaseMapper` 类型
- [ ] 每个 DAO 方法单表且无 join
- [ ] 跨表视图用独立分批查询 + 内存组装
- [ ] 跨表写与稳定键传播交给事务 service
- [ ] 请求与 VO 是不可变 record
- [ ] 集合组件已防御拷贝
- [ ] 枚举只表达本域
- [ ] 未创建投机性分层或空包

### 端点

- [ ] 端点边界内聚且经过刻意拆分
- [ ] Jakarta REST 路径与参数注解显式
- [ ] 每个方法返回共享响应包装
- [ ] 推迟的方法用 `TODO` + `UnsupportedOperationException`
- [ ] 端点方法不含持久化或工作流实现

### 验证

- [ ] 契约测试在实现前被观察到红灯
- [ ] 聚焦测试通过
- [ ] Java 改动后 `mvn clean compile` 通过
- [ ] `mvn validate` 通过
- [ ] `mvn test` 通过
- [ ] `mvn -q help:effective-pom` 通过
- [ ] `git diff --check` 通过
- [ ] 未生成或修改任何技能文档
