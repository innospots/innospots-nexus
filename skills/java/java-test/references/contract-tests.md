# 契约测试写法

契约测试锁住的是**结构形状**：注解、路径、签名、命名、状态码格式、索引。
它们通常在实现之前写成，先红灯，再实现转绿。

---

## 实体契约测试

目标：锁住表名、基类、主键、必填字段、长度、可空性、索引。

```java
package com.innospots.nexus.kernel.role.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class RoleEntityContractsTest {

    @Test
    void declaresNexusTableWithConstantName() {
        assertThat(RoleEntity.TABLE_NAME).isEqualTo("nx_role");

        Table table = RoleEntity.class.getAnnotation(Table.class);
        assertThat(table).isNotNull();
        assertThat(table.name()).isEqualTo(RoleEntity.TABLE_NAME);

        assertThat(RoleEntity.class.getAnnotation(Entity.class)).isNotNull();
    }

    @Test
    void extendsWorkspaceScopedBaseEntity() {
        assertThat(WorkspaceBaseEntity.class).isAssignableFrom(RoleEntity.class);
    }

    @Test
    void declaresUuidAssignedStringPrimaryKey() throws Exception {
        Field roleId = RoleEntity.class.getDeclaredField("roleId");

        assertThat(roleId.getType()).isEqualTo(String.class);
        assertThat(roleId.getAnnotation(TableId.class).type()).isEqualTo(IdType.ASSIGN_UUID);
        assertThat(roleId.getAnnotation(Id.class)).isNotNull();

        Column column = roleId.getAnnotation(Column.class);
        assertThat(column.length()).isEqualTo(32);
        assertThat(column.nullable()).isFalse();
    }

    @Test
    void usesPowerOfTwoStringLengths() {
        assertThat(RoleEntity.class.getDeclaredFields())
                .filteredOn(field -> field.getType() == String.class)
                .allSatisfy(field -> {
                    Column column = field.getAnnotation(Column.class);
                    if (column == null) {
                        return;
                    }
                    assertThat(column.length())
                            .as("length of %s", field.getName())
                            .isIn(16, 32, 64, 128, 256, 512, 1024);
                });
    }

    @Test
    void declaresOwnerAwareUniqueIndex() {
        Table table = RoleEntity.class.getAnnotation(Table.class);

        assertThat(table.indexes())
                .anySatisfy(index -> {
                    assertThat(index.unique()).isTrue();
                    assertThat(index.columnList()).contains("workspace_id");
                    assertThat(index.name()).startsWith("uk_");
                });
    }

    @Test
    void overridesStableIdPrefix() {
        assertThat(new RoleEntity().idPrefix()).isEqualTo("rol");
    }
}
```

要点：索引名以 `uk_` / `idx_` 开头并带表概念；工作区唯一性索引含 `workspace_id`，
租户唯一性索引含 `tenant_id`。

---

## DAO 契约测试

目标：锁住 `BaseMapper` 泛型绑定与单表约束。

```java
package com.innospots.nexus.kernel.role.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import static org.assertj.core.api.Assertions.assertThat;

class RoleDaoContractsTest {

    @Test
    void bindsToTheRoleEntity() {
        ParameterizedType mapperType = (ParameterizedType)
                (Arrays.stream(RoleDao.class.getGenericInterfaces())
                        .filter(type -> type instanceof ParameterizedType)
                        .filter(type -> ((ParameterizedType) type).getRawType() == BaseMapper.class)
                        .findFirst()
                        .orElseThrow());

        assertThat(mapperType.getActualTypeArguments()[0]).isEqualTo(RoleEntity.class);
    }

    @Test
    void usesLambdaWrappersInsteadOfSqlJoins() {
        assertThat(RoleDao.class.getMethods())
                .filteredOn(method -> method.isDefault())
                .allSatisfy(method -> assertThat(method.getName()).doesNotContain("join"));
    }
}
```

单表与无 join 主要靠代码评审保证；可辅以 SQL 日志断言或专门的集成测试。

---

## 端点契约测试

目标：锁住类形态、路径、HTTP 注解、返回包装、推迟行为。

```java
package com.innospots.nexus.kernel.role.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

import com.innospots.nexus.base.domain.response.R;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleEndpointContractsTest {

    @Test
    void declaresJsonResourcePath() {
        Path path = RoleEndpoint.class.getAnnotation(Path.class);

        assertThat(path.value()).isEqualTo("/roles");
        assertThat(RoleEndpoint.class.getAnnotation(Produces.class).value())
                .containsExactly(MediaType.APPLICATION_JSON);
        assertThat(RoleEndpoint.class.getAnnotation(Consumes.class).value())
                .containsExactly(MediaType.APPLICATION_JSON);
    }

    @Test
    void returnsSharedResponseWrapperFromEveryMethod() {
        assertThat(RoleEndpoint.class.getDeclaredMethods())
                .allSatisfy(method -> assertThat(method.getGenericReturnType())
                        .isInstanceOf(ParameterizedType.class))
                .allSatisfy(method -> assertThat(
                        ((ParameterizedType) method.getGenericReturnType()).getRawType())
                        .isEqualTo(R.class));
    }

    @Test
    void annotatesPathParametersExplicitly() throws Exception {
        Method getRole = RoleEndpoint.class.getMethod("getRole", String.class);

        assertThat(getRole.getAnnotation(GET.class)).isNotNull();
        assertThat(getRole.getAnnotation(Path.class).value()).isEqualTo("/{roleId}");
        assertThat(getRole.getParameters()[0].getAnnotation(PathParam.class)).isNotNull();
    }

    @Test
    void failsExplicitlyWhenBehaviorIsDeferred() {
        RoleEndpoint endpoint = new RoleEndpoint(null, null);

        assertThatThrownBy(() -> endpoint.getRole("rol-1"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Role lookup is not implemented");
    }

    @Test
    void doesNotUseSpringMvcAnnotations() {
        assertThat(Arrays.stream(RoleEndpoint.class.getAnnotations())
                        .map(annotation -> annotation.annotationType().getName()))
                .noneMatch(name -> name.startsWith("org.springframework.web.bind.annotation"));
    }
}
```

---

## 状态码契约测试

`standards/exception-status-code.md` 第 8 节要求的 13 项必须被证明：

| # | 必测性质 |
|---|---------|
| 1 | 模块是三个大写字母 |
| 2 | 类别存在且属于预期的语义族 |
| 3 | 本地码是四位数字 |
| 4 | 全码九字符，且等于 `module + category + local` |
| 5 | 本地码与全码在所属模块目录内唯一 |
| 6 | 枚举常量 `UPPER_SNAKE_CASE`，类型以 `StatusCode` 结尾 |
| 7 | 非成功状态的英文与中文 message/advice 均非空；有意的 success/no-advice 状态豁免，遗留 locale 回退需记录 |
| 8 | 类别 `label()` 与 `priority()` 是刻意且稳定的 |
| 9 | HTTP 映射是刻意的，且与边界契约一致 |
| 10 | `NexusException.build(status)` 返回预期 `code()` |
| 11 | `NexusException.build(status, cause)` 保留 cause |
| 12 | raw-code interop 拒绝畸形或未登记的码 |
| 13 | 端点基础设施把状态映射为 `R.fail(...)`，且不暴露 cause 或堆栈 |
| 14 | 模块/包归属与同级模块依赖规则被遵守（如 kernel 与 platform 不互引） |

```java
package com.innospots.nexus.kernel.role.domain.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.StatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleStatusCodeContractsTest {

    @Test
    void buildsNineCharacterFullCode() {
        StatusCode status = RoleStatusCode.ROLE_NOT_FOUND;

        assertThat(status.module()).matches("[A-Z]{3}");
        assertThat(status.localCode()).matches("\\d{4}");
        assertThat(status.fullCode()).hasSize(9);
        assertThat(status.fullCode()).isEqualTo(status.bisCode());
        assertThat(status.fullCode())
                .isEqualTo(status.module() + status.category().code() + status.localCode());
    }

    @Test
    void keepsLocalCodesUniqueWithinTheModule() {
        assertThat(Arrays.stream(RoleStatusCode.values()).map(RoleStatusCode::localCode).distinct().count())
                .isEqualTo(RoleStatusCode.values().length);
        assertThat(Arrays.stream(RoleStatusCode.values()).map(RoleStatusCode::fullCode).distinct().count())
                .isEqualTo(RoleStatusCode.values().length);
    }

    @Test
    void providesBilingualMessageAndAdvice() {
        assertThat(Arrays.stream(RoleStatusCode.values())
                        .filter(status -> status != RoleStatusCode.SUCCESS))
                .allSatisfy(status -> {
                    assertThat(status.message().enValue()).isNotBlank();
                    assertThat(status.message().cnValue()).isNotBlank();
                    assertThat(status.advice().enValue()).isNotBlank();
                    assertThat(status.advice().cnValue()).isNotBlank();
                });
    }

    @Test
    void mapsIntentionalHttpStatus() {
        assertThat(RoleStatusCode.ROLE_NOT_FOUND.httpStatusCode()).isEqualTo(404);
    }

    @Test
    void preservesCauseWhenTranslatingInfrastructureFailures() {
        IllegalStateException cause = new IllegalStateException("connection reset");
        NexusException exception = NexusException.build(RoleStatusCode.ROLE_NOT_FOUND, cause);

        assertThat(exception.status().fullCode()).isEqualTo(RoleStatusCode.ROLE_NOT_FOUND.fullCode());
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
```

> 上例的 `status.message().enValue()`、`status.category().code()` 等取自 base 现有实现。
> 写测试前先读 `com.innospots.nexus.base.status.StatusCode` 确认当前签名。

### 敏感信息测试

```java
@Test
void keepsSecretsOutOfStatusText() {
    assertThat(RoleStatusCode.values())
            .allSatisfy(status -> {
                assertThat(status.message().enValue()).doesNotContain("password", "token", "secret");
                assertThat(status.advice().enValue()).doesNotContain("password", "token", "secret");
            });
}
```

---

## 领域行为测试

### 校验与不变量

```java
@Test
void createRejectsMissingRoleName() {
    RoleCreateRequest request = new RoleCreateRequest("ADMIN", "  ", null);

    assertThatThrownBy(request::validate)
            .isInstanceOf(NexusException.class)
            .extracting(exception -> ((NexusException) exception).status().fullCode())
            .isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
}
```

### 集合不可变性与防御拷贝

```java
@Test
void defensivelyCopiesCollectionComponents() {
    List<String> tags = new ArrayList<>(List.of("ops"));
    RoleCreateRequest request = new RoleCreateRequest("ADMIN", "Admin", tags);

    tags.add("finance");

    assertThat(request.tags()).containsExactly("ops");
    assertThatThrownBy(() -> request.tags().add("x"))
            .isInstanceOf(UnsupportedOperationException.class);
}
```

### 查询与命令语义

```java
@Test
void createRejectsDuplicateStableKey() {
    roleDao.insert(existingRole("ADMIN"));

    assertThatThrownBy(() -> roleService.createRole(new RoleCreateRequest("ADMIN", "Admin", null)))
            .isInstanceOf(NexusException.class);

    assertThat(roleDao.selectByRoleCode("ADMIN")).isNotNull();
}
```

### 幂等与重复调用

```java
@Test
void repeatedStartIsIdempotent() {
    manager.start();
    manager.start();

    assertThat(manager.state()).isEqualTo(PluginState.ACTIVE);
}
```

### 跨表组装不是 N+1

用可观测的查询计数器（测试替身 DAO 或 SQL 计数）断言查询次数与批次数量级无关：

```java
@Test
void assemblesCrossTableViewInBatches() {
    CountingRoleDao countingDao = new CountingRoleDao();

    new RoleMemberService(countingDao, memberDao, userDao).listMemberViews(List.of("r1", "r2", "r3"));

    assertThat(countingDao.queryCount()).isLessThanOrEqualTo(3);
}
```

---

## 契约测试组织约定

| 约定 | 说明 |
|------|------|
| 包级私有类 | 不加 `public` |
| 方法名行为短语 | 无 `test` 前缀 |
| 每个测试一个契约点 | 失败时能直接定位到具体约束 |
| 断言带 `as(...)` 说明 | 反射循环中断言尤其需要 |
| 先确认红灯 | 实现前先跑一次，确认因缺少契约而失败 |
| 不削弱断言 | 测试失败暴露既有无关问题时，区分处理而不是放宽断言 |
