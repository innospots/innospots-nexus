package com.innospots.nexus.kernel.member.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.TenantBaseEntity;
import com.innospots.nexus.kernel.member.domain.enums.TenantMemberStatus;

import static org.assertj.core.api.Assertions.assertThat;

class TenantMemberEntityContractsTest {

    @Test
    void tenantMemberEntityExposesTenantScopedPersistenceTable() {
        assertPersistenceTable(TenantMemberEntity.class, "nx_tenant_member");
        assertThat(TenantMemberEntity.class.getSuperclass()).isEqualTo(TenantBaseEntity.class);
        assertThat(new TenantMemberEntity().idPrefix()).isEqualTo("tmb");
    }

    @Test
    void tenantMemberEntityContainsMembershipFields() throws NoSuchFieldException {
        assertPersistenceId(TenantMemberEntity.class.getDeclaredField("tenantMemberId"));
        assertField(TenantMemberEntity.class, "tenantMemberId", String.class, 32, false);
        assertField(TenantMemberEntity.class, "tenantUserId", String.class, 32, false);
        assertField(TenantMemberEntity.class, "status", String.class, 32, false);
        Field joinedAt = TenantMemberEntity.class.getDeclaredField("joinedAt");
        assertThat(joinedAt.getType()).isEqualTo(LocalDateTime.class);
        assertThat(joinedAt.getAnnotation(Column.class).nullable()).isFalse();
    }

    @Test
    void tenantMemberStatusEnumeratesMembershipLifecycle() {
        assertThat(TenantMemberStatus.values()).containsExactly(
                TenantMemberStatus.ACTIVE, TenantMemberStatus.DISABLED, TenantMemberStatus.PENDING);
    }

    @Test
    void tenantMemberEntityDeclaresUserUniqueIndex() {
        assertIndex(TenantMemberEntity.class, "uk_nx_tenant_member_user", "tenant_id,tenant_user_id", true);
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
