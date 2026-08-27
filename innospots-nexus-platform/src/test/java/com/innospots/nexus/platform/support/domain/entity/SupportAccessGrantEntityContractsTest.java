package com.innospots.nexus.platform.support.domain.entity;

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
import com.innospots.nexus.platform.support.domain.enums.SupportAccessStatus;

import static org.assertj.core.api.Assertions.assertThat;

class SupportAccessGrantEntityContractsTest {

    @Test
    void supportAccessGrantExposesGlobalPersistenceTable() {
        assertPersistenceTable(SupportAccessGrantEntity.class, "nx_support_access_grant");
        assertThat(SupportAccessGrantEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(new SupportAccessGrantEntity().idPrefix()).isEqualTo("sag");
        assertThat(SupportAccessStatus.values()).containsExactly(
                SupportAccessStatus.PENDING,
                SupportAccessStatus.ACTIVE,
                SupportAccessStatus.EXPIRED,
                SupportAccessStatus.REVOKED);
    }

    @Test
    void supportAccessGrantContainsLifecycleFields() throws NoSuchFieldException {
        assertPersistenceId(SupportAccessGrantEntity.class.getDeclaredField("grantId"));
        assertField(SupportAccessGrantEntity.class, "grantId", String.class, 32, false);
        assertField(SupportAccessGrantEntity.class, "tenantId", String.class, 32, false);
        assertField(SupportAccessGrantEntity.class, "platformUserId", String.class, 32, false);
        assertField(SupportAccessGrantEntity.class, "reason", String.class, 512, false);
        assertField(SupportAccessGrantEntity.class, "approvedBy", String.class, 32, true);
        Field expireAt = SupportAccessGrantEntity.class.getDeclaredField("expireAt");
        assertThat(expireAt.getType()).isEqualTo(LocalDateTime.class);
        assertThat(expireAt.getAnnotation(Column.class)).isNotNull();
        assertField(SupportAccessGrantEntity.class, "status", String.class, 32, false);
    }

    @Test
    void supportAccessGrantDeclaresLookupIndexes() {
        assertIndex(SupportAccessGrantEntity.class, "idx_nx_support_access_tenant", "tenant_id", false);
        assertIndex(SupportAccessGrantEntity.class, "idx_nx_support_access_user", "platform_user_id", false);
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
