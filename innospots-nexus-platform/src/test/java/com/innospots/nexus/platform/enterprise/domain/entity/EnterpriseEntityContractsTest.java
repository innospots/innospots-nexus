package com.innospots.nexus.platform.enterprise.domain.entity;

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

import static org.assertj.core.api.Assertions.assertThat;

class EnterpriseEntityContractsTest {

    @Test
    void enterpriseEntityExposesGlobalPersistenceTable() {
        assertPersistenceTable(EnterpriseEntity.class, "nx_enterprise");
        assertThat(EnterpriseEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(new EnterpriseEntity().idPrefix()).isEqualTo("ent");
    }

    @Test
    void enterpriseEntityContainsLegalProfileFields() throws NoSuchFieldException {
        assertPersistenceId(EnterpriseEntity.class.getDeclaredField("enterpriseId"));
        assertField(EnterpriseEntity.class, "enterpriseId", String.class, 32, false);
        assertField(EnterpriseEntity.class, "tenantId", String.class, 32, false);
        assertField(EnterpriseEntity.class, "legalName", String.class, 256, false);
        assertField(EnterpriseEntity.class, "creditCode", String.class, 64, true);
        assertField(EnterpriseEntity.class, "industry", String.class, 64, true);
        assertField(EnterpriseEntity.class, "contactName", String.class, 128, true);
        assertField(EnterpriseEntity.class, "contactPhone", String.class, 32, true);
        assertField(EnterpriseEntity.class, "contactEmail", String.class, 128, true);
        assertField(EnterpriseEntity.class, "address", String.class, 512, true);
        assertField(EnterpriseEntity.class, "extra", String.class, 1024, true);
    }

    @Test
    void enterpriseEntityDeclaresOneToOneTenantIndex() {
        assertIndex(EnterpriseEntity.class, "uk_nx_enterprise_tenant", "tenant_id", true);
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
