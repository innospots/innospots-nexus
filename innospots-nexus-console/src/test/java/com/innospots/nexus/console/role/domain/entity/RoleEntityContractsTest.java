package com.innospots.nexus.console.role.domain.entity;

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

import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RoleEntityContractsTest {

    @Test
    void roleEntitiesExposeOwnerScopedPersistenceTables() {
        assertPersistenceTable(RoleEntity.class, "nx_role");
        assertPersistenceTable(RoleBindingEntity.class, "nx_role_binding");

        assertThat(RoleEntity.class.getSuperclass()).isEqualTo(WorkspaceBaseEntity.class);
        assertThat(RoleBindingEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(new RoleBindingEntity().idPrefix()).isEqualTo("rbn");
    }

    @Test
    void roleEntityContainsOwnershipFields() throws NoSuchFieldException {
        assertPersistenceId(RoleEntity.class.getDeclaredField("roleId"));
        assertField(RoleEntity.class, "roleId", String.class, 32, false);
        assertField(RoleEntity.class, "roleName", String.class, 64, false);
        assertField(RoleEntity.class, "roleCode", String.class, 64, false);
        assertField(RoleEntity.class, "ownerType", String.class, 32, false);
        assertField(RoleEntity.class, "ownerId", String.class, 32, true);
        assertField(RoleEntity.class, "securityRealm", String.class, 32, false);
        assertField(RoleEntity.class, "description", String.class, 256, true);
        assertField(RoleEntity.class, "status", String.class, 32, false);
        assertField(RoleEntity.class, "sortOrder", Integer.class, 255, false);
        assertField(RoleEntity.class, "builtIn", Boolean.class, 255, false);
        assertField(RoleEntity.class, "administrator", Boolean.class, 255, false);
        assertThat(RoleOwnerType.values()).containsExactly(
                RoleOwnerType.PLATFORM, RoleOwnerType.TENANT, RoleOwnerType.WORKSPACE);
    }

    @Test
    void roleBindingEntityContainsSubjectKeys() throws NoSuchFieldException {
        assertPersistenceId(RoleBindingEntity.class.getDeclaredField("bindingId"));
        assertField(RoleBindingEntity.class, "bindingId", String.class, 32, false);
        assertField(RoleBindingEntity.class, "roleId", String.class, 32, false);
        assertField(RoleBindingEntity.class, "subjectType", String.class, 32, false);
        assertField(RoleBindingEntity.class, "subjectId", String.class, 32, false);
        assertThat(RoleBindingSubjectType.values()).containsExactly(
                RoleBindingSubjectType.USER, RoleBindingSubjectType.ORG_UNIT);
    }

    @Test
    void roleEntitiesDeclareOwnerAwareIndexes() {
        assertIndex(RoleEntity.class, "uk_nx_role_owner_code", "owner_type,owner_id,role_code", true);
        assertIndex(RoleEntity.class, "idx_nx_role_workspace_status", "workspace_id,status", false);
        assertIndex(RoleEntity.class, "idx_nx_role_realm", "security_realm", false);
        assertIndex(RoleBindingEntity.class, "uk_nx_role_binding_subject",
                "role_id,subject_type,subject_id", true);
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
