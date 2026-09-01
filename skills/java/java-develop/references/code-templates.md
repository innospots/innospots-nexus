# 代码骨架模板

以下模板已满足 `standards/` 的主要约束，可直接套用后按领域替换名称。
导入顺序：`java.*`/`jakarta.*` → 第三方（含 Lombok）→ `com.innospots.*`。

---

## 持久化实体

```java
package com.innospots.nexus.kernel.role.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;

/**
 * A role owned by one workspace. The {@code roleCode} is a stable business key
 * that stays immutable after creation and is unique within the owning workspace.
 */
@Getter
@Setter
@Entity
@Table(name = RoleEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_role_owner_code", columnList = "owner_id,role_code", unique = true),
        @Index(name = "idx_nx_role_status", columnList = "status")
})
@TableName(RoleEntity.TABLE_NAME)
public class RoleEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_role";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String roleId;

    @Column(length = 64, nullable = false)
    private String roleCode;

    @Column(length = 128, nullable = false)
    private String roleName;

    @Column(length = 512)
    private String description;

    @Column(length = 32)
    private String status;

    @Override
    public String idPrefix() {
        return "rol";
    }
}
```

要点核对：

- 继承 `WorkspaceBaseEntity` / `TenantBaseEntity` / `BaseEntity`（三选一）
- 主键 `String` + 三注解齐全 + `length = 32`
- `TABLE_NAME` 常量同时用于 `@Table` 与 `@TableName`
- 字符串长度均为 2 的幂；无界文本用 `@Lob`
- Lombok `@Getter` + `@Setter`（不是 `@Data`）
- 覆写 `idPrefix()`，短小稳定
- 索引名 `uk_` / `idx_` + 表概念 + 用途
- 不重复声明 `tenantId` / `workspaceId` / `createdAt` / `updatedAt` / `createdBy` / `updatedBy`

---

## DAO

```java
package com.innospots.nexus.kernel.role.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.kernel.role.domain.entity.RoleEntity;

import java.util.List;

/**
 * Single-table persistence access for workspace roles.
 */
public interface RoleDao extends BaseMapper<RoleEntity> {

    /**
     * Finds a role by its stable code.
     *
     * @param roleCode stable role code
     * @return matching role or {@code null} when absent
     */
    default RoleEntity selectByRoleCode(String roleCode) {
        return selectOne(Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getRoleCode, roleCode));
    }

    /**
     * Lists roles in the given status set.
     *
     * @param statuses role statuses to include
     * @return matching roles, empty when none
     */
    default List<RoleEntity> selectByStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        return selectList(Wrappers.<RoleEntity>lambdaQuery()
                .in(RoleEntity::getStatus, statuses));
    }
}
```

要点核对：

- `dao` 包、`*Dao` 后缀、`extends BaseMapper<EntityType>`
- 自定义操作是 `default` 方法 + `Wrappers.lambdaQuery()` / `lambdaUpdate()`
- 用 lambda 方法引用而非字符串列名
- 每个方法只访问一张表，**无 join**
- 可空返回在 Javadoc 中声明
- **不得**创建 Mapper XML

### 跨表读取的组装模式

```java
// 1) 查主表  2) 收集标识  3) 分批查各表  4) 内存映射  5) 返回组装视图
List<RoleMemberEntity> members = roleMemberDao.selectByRoleIds(roleIds);
List<String> userIds = members.stream().map(RoleMemberEntity::getUserId).distinct().toList();
Map<String, KernelUserEntity> usersById = kernelUserDao.selectByUserIds(userIds).stream()
        .collect(Collectors.toMap(KernelUserEntity::getUserId, Function.identity()));
```

禁止 N+1：不得对上一批结果的每一行再查一次关联表。

---

## 请求记录（Request）

```java
package com.innospots.nexus.kernel.role.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * Creates a role inside the current workspace.
 *
 * @param roleCode stable role code, immutable after creation
 * @param roleName display name
 * @param description optional description
 */
public record RoleCreateRequest(String roleCode, String roleName, String description) {

    /**
     * Validates required role attributes.
     */
    public void validate() {
        if (roleCode == null || roleCode.isBlank()) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
        if (roleName == null || roleName.isBlank()) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
    }
}
```

```java
package com.innospots.nexus.kernel.role.domain.request;

import java.util.List;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

/**
 * Paginated role query.
 */
public record RolePageRequest(
        @QueryParam("input") String input,
        @QueryParam("status") String status,
        @DefaultValue("1") @QueryParam("pageNo") Integer pageNo,
        @DefaultValue("20") @QueryParam("pageSize") Integer pageSize
) {

    public RolePageRequest {
        // Normalize invalid pagination to shared defaults before it becomes stable state.
    }

    /**
     * Returns the effective page number.
     *
     * @return one-based page number, never below one
     */
    public int effectivePageNo() {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }
}
```

要点核对：

- 必须是 record
- 更新请求排除不可变稳定键与受保护系统字段
- 集合组件在紧凑构造器中 `List.copyOf` / `Set.copyOf` / `Map.copyOf`
- 查询参数显式 `@QueryParam`，必要时 `@DefaultValue`
- 分页值归一化到共享默认值
- 校验方法拒绝输入时抛 `NexusException`；布尔探针用 `isValid` 且不得改状态

---

## 视图记录（VO）

```java
package com.innospots.nexus.kernel.role.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Primary management representation of a role.
 *
 * @param roleId technical identifier
 * @param roleCode stable business key
 * @param roleName display name
 * @param status business availability status
 * @param createdAt creation timestamp
 */
public record RoleVo(
        String roleId,
        String roleCode,
        String roleName,
        String status,
        LocalDateTime createdAt
) {
}
```

```java
package com.innospots.nexus.kernel.role.domain.vo;

import java.util.List;

/**
 * Compact projection used by role selectors.
 */
public record RoleOptionVo(String roleId, String roleCode, String roleName) {

    /**
     * Creates an immutable option list from the given roles.
     *
     * @param roles source roles
     * @return immutable option list
     */
    public static List<RoleOptionVo> of(List<RoleVo> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(role -> new RoleOptionVo(role.roleId(), role.roleCode(), role.roleName()))
                .toList();
    }
}
```

要点核对：

- 必须是 record
- 集合元素类型用单数
- 子视图与集合字段防御拷贝
- 不直接暴露持久化实体
- 禁用大写 `VO` / `Dto` / `Response` / `Result`

---

## 领域枚举与状态码

```java
package com.innospots.nexus.kernel.role.domain.enums;

/**
 * Business availability of a role.
 */
public enum RoleStatus {

    /** Available for assignment. */
    ENABLED,

    /** Retained but not assignable. */
    DISABLED
}
```

```java
package com.innospots.nexus.kernel.role.domain.enums;

import com.innospots.nexus.base.status.StatusCode;
import com.innospots.nexus.base.status.StatusCategory;
import org.springframework.http.HttpStatus; // 仅示例，按实际 HTTP 映射常量来源调整

/**
 * Role-domain failure codes.
 */
public enum RoleStatusCode implements StatusCode {

    ROLE_NOT_FOUND("0001", StatusCategory.RESOURCE_DATA);

    private final String localCode;
    private final StatusCategory category;

    RoleStatusCode(String localCode, StatusCategory category) {
        this.localCode = localCode;
        this.category = category;
    }

    @Override
    public String module() {
        return "ROL";
    }

    @Override
    public String localCode() {
        return localCode;
    }

    @Override
    public StatusCategory category() {
        return category;
    }

    @Override
    public String message() {
        return "The requested role does not exist.";
    }

    @Override
    public String advice() {
        return "请确认角色标识是否正确，或联系管理员重新分配角色。";
    }

    @Override
    public int httpStatusCode() {
        return 404;
    }
}
```

要点核对：

- 类型名 `XxxStatusCode`，实现 `StatusCode`
- 全码 = `MODULE(3) + CATEGORY(2) + LOCAL(4)` 共 9 字符，`bisCode()` == `fullCode()`
- 类别按失败语义选择，不因 HTTP 映射方便而选
- 非成功状态提供有意义的中英文 message 与 advice
- 文本中不含密钥、ID、用户输入、SQL、路径、堆栈
- 新增前先搜索现有目录；分配未使用的本地码，不得重排既有码

> 上例的 `module()`、`localCode()`、`category()` 等方法是**示意签名**，
> 实际以 `com.innospots.nexus.base.status.StatusCode` 的当前接口为准——写之前先读源码。

---

## MapStruct 转换器

```java
package com.innospots.nexus.kernel.role.converter;

import org.mapstruct.Mapper;

import com.innospots.nexus.base.mapstruct.BaseBeanConverter;
import com.innospots.nexus.base.mapstruct.BaseMapperConfig;
import com.innospots.nexus.kernel.role.domain.entity.RoleEntity;
import com.innospots.nexus.kernel.role.domain.model.Role;
import com.innospots.nexus.kernel.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.kernel.role.domain.vo.RoleVo;

/**
 * Structural conversion among role request, model, entity, and view types.
 */
@Mapper(config = BaseMapperConfig.class)
public interface RoleConverter extends BaseBeanConverter<Role, RoleEntity> {

    RoleVo modelToVo(Role model);

    Role requestToModel(RoleCreateRequest request);
}
```

要点核对：

- `converter` 包、`*Converter` 后缀
- `@Mapper(config = BaseMapperConfig.class)`
- model ↔ entity 继承 `BaseBeanConverter<Model, Entity>`
- 端点 / service / operator 中不得有大段逐字段拷贝
- 一两个标量值的局部映射不必建转换器

---

## Operator

```java
package com.innospots.nexus.kernel.role.operator;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.kernel.role.dao.RoleDao;
import com.innospots.nexus.kernel.role.domain.entity.RoleEntity;
import com.innospots.nexus.kernel.role.domain.enums.RoleStatusCode;

/**
 * Direct data operations over the role table.
 */
@Slf4j
@RequiredArgsConstructor
public final class RoleOperator {

    private final RoleDao roleDao;

    /**
     * Loads a role by identifier.
     *
     * @param roleId role identifier
     * @return matching role
     * @throws com.innospots.nexus.base.exception.NexusException when the role is absent
     */
    public RoleEntity requireRole(String roleId) {
        RoleEntity role = roleDao.selectById(roleId);
        if (role == null) {
            throw NexusException.build(RoleStatusCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    /**
     * Lists roles matching the given status.
     *
     * @param status business availability status
     * @return matching roles, empty when none
     */
    public List<RoleEntity> listByStatus(String status) {
        List<RoleEntity> roles = roleDao.selectByStatuses(List.of(status));
        log.debug("Listed {} roles in status {}", roles.size(), status);
        return roles;
    }
}
```

要点核对：

- 构造器注入 + `final` 字段 + `@RequiredArgsConstructor`
- `@Slf4j`（不是 `@Sl4j`），不手写 logger
- **不得**依赖 service 或另一个 operator
- 把 DAO 的可空缺失翻译成恰当状态码
- 集合结果不返回 `null`

---

## Service

```java
package com.innospots.nexus.kernel.role.service;

import java.util.List;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.kernel.role.converter.RoleConverter;
import com.innospots.nexus.kernel.role.dao.RoleDao;
import com.innospots.nexus.kernel.role.domain.entity.RoleEntity;
import com.innospots.nexus.kernel.role.domain.enums.RoleStatusCode;
import com.innospots.nexus.kernel.role.domain.model.Role;
import com.innospots.nexus.kernel.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.kernel.role.operator.RoleOperator;

/**
 * Role lifecycle workflows that coordinate validation, persistence, and events.
 */
@Slf4j
@RequiredArgsConstructor
public final class RoleService {

    private final RoleDao roleDao;
    private final RoleOperator roleOperator;
    private final RoleConverter roleConverter;

    /**
     * Creates a role after rejecting a duplicate stable key.
     *
     * @param request create request
     * @return created role model
     * @throws NexusException when the role code already exists in the workspace
     */
    @Transactional
    public Role createRole(RoleCreateRequest request) {
        request.validate();
        RoleEntity existing = roleDao.selectByRoleCode(request.roleCode());
        if (existing != null) {
            throw NexusException.build(RoleStatusCode.ROLE_CODE_DUPLICATED);
        }
        Role model = roleConverter.requestToModel(request);
        RoleEntity entity = roleConverter.modelToEntity(model);
        roleDao.insert(entity);
        log.info("Created role {}", entity.getRoleCode());
        return roleConverter.entityToModel(entity);
    }

    /**
     * Pages roles for management screens.
     *
     * @param pageNo one-based page number
     * @param pageSize page size
     * @return paginated role models
     */
    public PageResult<Role> pageRoles(int pageNo, int pageSize) {
        List<RoleEntity> entities = roleDao.selectList(null);
        return PageResult.of(entities, pageNo, pageSize);
    }
}
```

要点核对：

- 事务用 `jakarta.transaction.Transactional`，方法级优先
- 返回领域值或 `PageResult<T>`，**不返回 `R<T>`**
- `create` 遇重复稳定键失败，不静默转 update
- 只在增加有用上下文处记录日志，不逐层重复

> `PageResult.of(...)` 是示意调用，实际签名以 base 模块源码为准。

---

## 端点

```java
package com.innospots.nexus.kernel.role.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.kernel.role.converter.RoleConverter;
import com.innospots.nexus.kernel.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.kernel.role.domain.vo.RoleVo;
import com.innospots.nexus.kernel.role.service.RoleService;

/**
 * Role lifecycle operations exposed to the management console.
 */
@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class RoleEndpoint {

    private final RoleService roleService;
    private final RoleConverter roleConverter;

    /**
     * Returns one role.
     *
     * @param roleId role identifier
     * @return role details
     */
    @GET
    @Path("/{roleId}")
    public R<RoleVo> getRole(@PathParam("roleId") String roleId) {
        // TODO Delegate role lookup to RoleService once the query contract is defined.
        throw new UnsupportedOperationException("Role lookup is not implemented");
    }

    /**
     * Creates a role.
     *
     * @param request create request
     * @return created role details
     */
    @POST
    public R<RoleVo> createRole(RoleCreateRequest request) {
        // TODO Delegate creation to RoleService once the workflow exists.
        throw new UnsupportedOperationException("Role creation is not implemented");
    }
}
```

要点核对：

- `endpoint` 包、`*Endpoint` 后缀、默认具体类
- 只用 `jakarta.ws.rs` 注解
- 类级 `@Path` + `@Produces` + `@Consumes`，方法级 HTTP 注解
- 所有参数显式注解
- 每个方法返回 `R<XxxVo>` / `R<PageResult<XxxVo>>` / `R<Void>`
- 推迟实现：聚焦 `TODO` + `UnsupportedOperationException`，**不返回伪造数据**
- 端点不直接依赖 DAO

---

## 领域事件与处理器

```java
package com.innospots.nexus.kernel.role.domain.event;

import com.innospots.nexus.base.events.DomainEvent;

/**
 * Published after a role is successfully created.
 *
 * @param roleId technical role identifier
 * @param roleCode stable business key
 */
public record RoleCreatedEvent(String roleId, String roleCode) implements DomainEvent {

    @Override
    public String eventType() {
        return "role.created";
    }
}
```

```java
package com.innospots.nexus.kernel.audit.handler;

import com.innospots.nexus.base.events.EventHandler;
import com.innospots.nexus.kernel.role.domain.event.RoleCreatedEvent;

/**
 * Records an audit entry for a created role.
 */
public final class RoleCreatedEventHandler implements EventHandler<RoleCreatedEvent> {

    @Override
    public Object handle(RoleCreatedEvent event) {
        // Delegate to this consumer module's service or operator.
        return null;
    }
}
```

要点核对：

- 事件是不可变 record，放发布域 `domain.event`，实现 `DomainEvent`
- 事件类型串用稳定小写点分名
- 状态变更**成功后**才发布；通知用 `EventBus.publish`，仅在确实需要立即结果时用 `publishSync`
- 消费方在自身 `handler` 包定义 `XxxEventHandler`
- 注册方负责 `unsubscribe` 清理
- 事件里不得放 DAO、service、可变实体或基础设施对象

---

## 模板使用提醒

- 模板是骨架，**不是**可复制的最终实现；领域字段、索引、状态码需按实际情况设计
- 用到 base / core 的 API 前先读源码确认当前签名，不要凭模板里的示意签名直接编译
- 每批改动后 `mvn clean compile`
