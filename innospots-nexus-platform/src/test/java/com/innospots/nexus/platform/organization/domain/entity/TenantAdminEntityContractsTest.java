package com.innospots.nexus.platform.organization.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.platform.organization.domain.enums.TenantAdminRole;

import static org.assertj.core.api.Assertions.assertThat;

class TenantAdminEntityContractsTest {

    @Test
    void tenantAdminEntityExposesPersistenceTable() {
        assertPersistenceTable(TenantAdminEntity.class, "nx_pl_tenant_admin");
        assertThat(TenantAdminEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(new TenantAdminEntity().idPrefix()).isEqualTo("tad");
    }

    @Test
    void tenantAdminEntityContainsAdminFields() throws NoSuchFieldException {
        assertPersistenceId(TenantAdminEntity.class.getDeclaredField("tenantAdminId"));
        assertField(TenantAdminEntity.class, "tenantAdminId", String.class, 32, false);
        assertField(TenantAdminEntity.class, "tenantId", String.class, 32, false);
        assertField(TenantAdminEntity.class, "email", String.class, 128, false);
        assertField(TenantAdminEntity.class, "adminRole", String.class, 32, false);
        assertField(TenantAdminEntity.class, "tenantUserId", String.class, 32, true);
        assertField(TenantAdminEntity.class, "displayName", String.class, 128, true);
    }

    @Test
    void tenantAdminRoleDefinesOwnerAndAdministrator() {
        assertThat(TenantAdminRole.values()).containsExactly(TenantAdminRole.OWNER, TenantAdminRole.ADMINISTRATOR);
    }

    @Test
    void tenantAdminEntityDeclaresTenantEmailUniqueIndex() {
        assertIndex(TenantAdminEntity.class, "uk_nx_pl_tenant_admin_email", "tenant_id,email", true);
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
        assertThat(field.getType()).isEqualTo(fieldType);
        Column column = field.getAnnotation(Column.class);
        assertThat(column.length()).isEqualTo(length);
        assertThat(column.nullable()).isEqualTo(nullable);
    }
}
