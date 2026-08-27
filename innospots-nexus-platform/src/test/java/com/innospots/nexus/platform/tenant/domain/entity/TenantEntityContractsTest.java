package com.innospots.nexus.platform.tenant.domain.entity;

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
import com.innospots.nexus.platform.tenant.domain.enums.TenantStatus;

import static org.assertj.core.api.Assertions.assertThat;

class TenantEntityContractsTest {

    @Test
    void tenantEntityExposesGlobalPersistenceTable() {
        assertPersistenceTable(TenantEntity.class, "nx_tenant");
        assertThat(TenantEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(new TenantEntity().idPrefix()).isEqualTo("tnt");
    }

    @Test
    void tenantEntityContainsLifecycleFields() throws NoSuchFieldException {
        assertPersistenceId(TenantEntity.class.getDeclaredField("tenantId"));
        assertField(TenantEntity.class, "tenantId", String.class, 32, false);
        assertField(TenantEntity.class, "tenantName", String.class, 128, false);
        assertField(TenantEntity.class, "tenantCode", String.class, 64, false);
        assertField(TenantEntity.class, "status", String.class, 32, false);
        assertField(TenantEntity.class, "planCode", String.class, 64, true);
        assertField(TenantEntity.class, "ownerTenantUserId", String.class, 32, true);
    }

    @Test
    void tenantStatusEnumeratesOpsLifecycle() {
        assertThat(TenantStatus.values()).containsExactly(
                TenantStatus.ACTIVE, TenantStatus.SUSPENDED, TenantStatus.ARCHIVED);
    }

    @Test
    void tenantEntityDeclaresTenantCodeUniqueIndex() {
        assertIndex(TenantEntity.class, "uk_nx_tenant_code", "tenant_code", true);
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
