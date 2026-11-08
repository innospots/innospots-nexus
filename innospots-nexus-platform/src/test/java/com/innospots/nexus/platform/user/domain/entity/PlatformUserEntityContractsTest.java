package com.innospots.nexus.console.user.admin.domain.entity;

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
import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUserEntityContractsTest {

    @Test
    void platformUserEntitiesExposeOpsRealmPersistenceTables() {
        assertPersistenceTable(AdminUserEntity.class, "nx_admin_user");
        assertPersistenceTable(AdminUserOauthEntity.class, "nx_admin_user_oauth");
        assertThat(AdminUserEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(new AdminUserEntity().idPrefix()).isEqualTo("ADM");
        assertThat(new AdminUserOauthEntity().idPrefix()).isEqualTo("POI");
    }

    @Test
    void platformUserEntityContainsOpsIdentityFields() throws NoSuchFieldException {
        assertPersistenceId(AdminUserEntity.class.getDeclaredField("adminUserId"));
        assertField(AdminUserEntity.class, "adminUserId", String.class, 32, false);
        assertField(AdminUserEntity.class, "securityRealm", String.class, 32, false);
        assertField(AdminUserEntity.class, "loginName", String.class, 64, false);
        assertField(AdminUserEntity.class, "displayName", String.class, 128, true);
        assertField(AdminUserEntity.class, "email", String.class, 128, true);
        assertField(AdminUserEntity.class, "mobile", String.class, 32, true);
        assertField(AdminUserEntity.class, "employeeNo", String.class, 64, true);
        assertField(AdminUserEntity.class, "status", String.class, 32, false);
        assertField(AdminUserEntity.class, "lastLoginTime", LocalDateTime.class, 255, true);
        assertField(AdminUserEntity.class, "lastLoginIp", String.class, 64, true);
    }

    @Test
    void platformUserOauthBindsAdminUserId() throws NoSuchFieldException {
        assertField(AdminUserOauthEntity.class, "adminUserId", String.class, 32, false);
        assertIndex(AdminUserOauthEntity.class, "idx_nx_admin_user_oauth_user",
                "admin_user_id", false);
    }

    @Test
    void platformUserEntityDeclaresLoginNameUniqueIndex() {
        assertIndex(AdminUserEntity.class, "uk_nx_admin_user_login_name", "login_name", true);
        assertIndex(AdminUserEntity.class, "idx_nx_admin_user_status", "status", false);
        assertIndex(AdminUserEntity.class, "idx_nx_admin_user_realm", "security_realm", false);
    }

    @Test
    void platformUserStatusEnumeratesOpsLifecycle() {
        assertThat(AdminUserStatus.values()).containsExactly(
                AdminUserStatus.PENDING_ACTIVATION,
                AdminUserStatus.PENDING_APPROVAL,
                AdminUserStatus.ACTIVE,
                AdminUserStatus.DISABLED,
                AdminUserStatus.LOCKED);
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
