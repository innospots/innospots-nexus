package com.innospots.nexus.kernel.workspace.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.TenantBaseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceEntityContractsTest {

    @Test
    void workspaceEntityExposesTenantScopedPersistenceTable() {
        assertPersistenceTable(WorkspaceEntity.class, "nx_workspace");
        assertThat(WorkspaceEntity.class.getSuperclass()).isEqualTo(TenantBaseEntity.class);
        assertThat(new WorkspaceEntity().idPrefix()).isEqualTo("wks");
    }

    @Test
    void workspaceEntityContainsCollaborationFields() throws NoSuchFieldException {
        assertPersistenceId(WorkspaceEntity.class.getDeclaredField("workspaceId"));
        assertField(WorkspaceEntity.class, "workspaceId", String.class, 32, false);
        assertField(WorkspaceEntity.class, "workspaceName", String.class, 128, false);
        assertField(WorkspaceEntity.class, "workspaceCode", String.class, 64, false);
        assertField(WorkspaceEntity.class, "description", String.class, 512, true);
        assertField(WorkspaceEntity.class, "status", String.class, 32, false);
    }

    @Test
    void workspaceEntityDeclaresTenantCodeUniqueIndex() {
        assertIndex(WorkspaceEntity.class, "uk_nx_workspace_tenant_code", "tenant_id,workspace_code", true);
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
