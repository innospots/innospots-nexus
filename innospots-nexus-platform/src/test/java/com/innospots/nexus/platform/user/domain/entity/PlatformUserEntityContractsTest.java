package com.innospots.nexus.platform.user.domain.entity;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformUserEntityContractsTest {

    @Test
    void platformUserEntitiesExposeOpsRealmPersistenceTables() {
        assertPersistenceTable(PlatformUserEntity.class, "nx_platform_user");
        assertPersistenceTable(PlatformUserPasswordEntity.class, "nx_platform_user_password");
        assertPersistenceTable(PlatformUserOauthEntity.class, "nx_platform_user_oauth");
        assertThat(PlatformUserEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(new PlatformUserEntity().idPrefix()).isEqualTo("pus");
        assertThat(new PlatformUserPasswordEntity().idPrefix()).isEqualTo("ppc");
        assertThat(new PlatformUserOauthEntity().idPrefix()).isEqualTo("poi");
    }

    @Test
    void platformUserEntityContainsOpsIdentityFields() throws NoSuchFieldException {
        assertPersistenceId(PlatformUserEntity.class.getDeclaredField("platformUserId"));
        assertField(PlatformUserEntity.class, "platformUserId", String.class, 32, false);
        assertField(PlatformUserEntity.class, "loginName", String.class, 64, false);
        assertField(PlatformUserEntity.class, "displayName", String.class, 128, true);
        assertField(PlatformUserEntity.class, "email", String.class, 128, true);
        assertField(PlatformUserEntity.class, "mobile", String.class, 32, true);
        assertField(PlatformUserEntity.class, "employeeNo", String.class, 64, true);
        assertField(PlatformUserEntity.class, "status", String.class, 32, false);
        assertField(PlatformUserEntity.class, "lastLoginTime", LocalDateTime.class, 255, true);
        assertField(PlatformUserEntity.class, "lastLoginIp", String.class, 64, true);
    }

    @Test
    void platformUserPasswordAndOauthBindPlatformUserId() throws NoSuchFieldException {
        assertField(PlatformUserPasswordEntity.class, "platformUserId", String.class, 32, false);
        assertField(PlatformUserOauthEntity.class, "platformUserId", String.class, 32, false);
        assertIndex(PlatformUserPasswordEntity.class, "uk_nx_platform_user_password_user",
                "platform_user_id", true);
        assertIndex(PlatformUserOauthEntity.class, "idx_nx_platform_user_oauth_user",
                "platform_user_id", false);
    }

    @Test
    void platformUserEntityDeclaresLoginNameUniqueIndex() {
        assertIndex(PlatformUserEntity.class, "uk_nx_platform_user_login_name", "login_name", true);
        assertIndex(PlatformUserEntity.class, "idx_nx_platform_user_status", "status", false);
    }

    @Test
    void platformUserStatusEnumeratesOpsLifecycle() {
        assertThat(PlatformUserStatus.values()).containsExactly(
                PlatformUserStatus.ACTIVE, PlatformUserStatus.DISABLED, PlatformUserStatus.LOCKED);
    }

    private static void assertPersistenceTable(Class<?> entityType, String tableName) {
        assertThat(entityType.getAnnotation(Entity.class)).isNotNull();
        assertThat(entityType.getAnnotation(Table.class).name()).isEqualTo(tableName);
        assertThat(entityType.getAnnotation(TableName.class).value()).isEqualTo(tableName);
    }

    private static void assertIndex(Class<?> entityType, String indexName, String columnList, boolean unique) {
        assertThat(entityType.getAnnotation(Table.class).indexes())
                .filteredOn(index -> index.name().equals(indexName))
                .singleElement()
                .satisfies(index -> {
                    Index tableIndex = (Index) index;
                    assertThat(tableIndex.columnList()).isEqualTo(columnList);
                    assertThat(tableIndex.unique()).isEqualTo(unique);
                });
    }

    private static void assertPersistenceId(Field field) {
        assertThat(field.getAnnotation(Id.class)).isNotNull();
        assertThat(field.getAnnotation(TableId.class).type()).isEqualTo(IdType.ASSIGN_UUID);
    }

    private static void assertField(
            Class<?> entityType,
            String fieldName,
            Class<?> fieldType,
            int length,
            boolean nullable
    ) throws NoSuchFieldException {
        Field field = entityType.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);

        assertThat(field.getType()).isEqualTo(fieldType);
        assertThat(column).isNotNull();
        assertThat(column.length()).isEqualTo(length);
        assertThat(column.nullable()).isEqualTo(nullable);
    }
}
