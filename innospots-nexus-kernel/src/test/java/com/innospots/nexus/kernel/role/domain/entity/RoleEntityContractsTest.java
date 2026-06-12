package com.innospots.nexus.kernel.role.domain.entity;

import java.lang.reflect.Field;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.entity.ProjectBaseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RoleEntityContractsTest {

    @Test
    void roleEntitiesExposeProjectScopedPersistenceTables() {
        assertPersistenceTable(RoleEntity.class, "nx_role");
        assertPersistenceTable(UserRoleEntity.class, "nx_user_role");

        assertThat(RoleEntity.class.getSuperclass()).isEqualTo(ProjectBaseEntity.class);
        assertThat(UserRoleEntity.class.getSuperclass()).isEqualTo(ProjectBaseEntity.class);
    }

    @Test
    void roleEntityContainsManagementFields() throws NoSuchFieldException {
        assertPersistenceId(RoleEntity.class.getDeclaredField("roleId"));
        assertField(RoleEntity.class, "roleId", String.class, 32, false);
        assertField(RoleEntity.class, "roleName", String.class, 64, false);
        assertField(RoleEntity.class, "roleCode", String.class, 64, false);
        assertField(RoleEntity.class, "description", String.class, 256, true);
        assertField(RoleEntity.class, "status", String.class, 32, false);
        assertField(RoleEntity.class, "sortOrder", Integer.class, 255, false);
        assertField(RoleEntity.class, "builtIn", Boolean.class, 255, false);
        assertField(RoleEntity.class, "administrator", Boolean.class, 255, false);
    }

    @Test
    void userRoleEntityContainsStableForeignKeys() throws NoSuchFieldException {
        assertPersistenceId(UserRoleEntity.class.getDeclaredField("userRoleId"));
        assertField(UserRoleEntity.class, "userRoleId", String.class, 32, false);
        assertField(UserRoleEntity.class, "userId", String.class, 32, false);
        assertField(UserRoleEntity.class, "roleId", String.class, 32, false);
    }

    @Test
    void roleEntitiesDeclareProjectAwareIndexes() {
        assertIndex(RoleEntity.class, "uk_nx_role_project_code", "project_id,role_code", true);
        assertIndex(RoleEntity.class, "idx_nx_role_project_status", "project_id,status", false);
        assertIndex(UserRoleEntity.class, "uk_nx_user_role_project_user_role",
                "project_id,user_id,role_id", true);
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

    private static void assertField(Class<?> entityType, String fieldName, Class<?> fieldType, int length, boolean nullable)
            throws NoSuchFieldException {
        Field field = entityType.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);

        assertThat(field.getType()).isEqualTo(fieldType);
        assertThat(column).isNotNull();
        assertThat(column.length()).isEqualTo(length);
        assertThat(column.nullable()).isEqualTo(nullable);
    }
}
