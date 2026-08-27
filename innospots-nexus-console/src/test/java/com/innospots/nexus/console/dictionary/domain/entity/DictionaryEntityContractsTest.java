package com.innospots.nexus.console.dictionary.domain.entity;

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

import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryEntityContractsTest {

    @Test
    void dictionaryEntitiesExposeRealmScopedPersistenceTables() {
        assertPersistenceTable(DictionaryTypeEntity.class, "nx_dictionary_type");
        assertPersistenceTable(DictionaryItemEntity.class, "nx_dictionary_item");

        assertThat(DictionaryTypeEntity.class.getSuperclass()).isEqualTo(WorkspaceBaseEntity.class);
        assertThat(DictionaryItemEntity.class.getSuperclass()).isEqualTo(WorkspaceBaseEntity.class);
        assertThat(new DictionaryTypeEntity().idPrefix()).isEqualTo("dct");
        assertThat(new DictionaryItemEntity().idPrefix()).isEqualTo("dci");
    }

    @Test
    void dictionaryTypeEntityContainsCatalogFields() throws NoSuchFieldException {
        assertPersistenceId(DictionaryTypeEntity.class.getDeclaredField("dictionaryTypeId"));
        assertField(DictionaryTypeEntity.class, "dictionaryTypeId", String.class, 32, false);
        assertField(DictionaryTypeEntity.class, "typeCode", String.class, 64, false);
        assertField(DictionaryTypeEntity.class, "typeName", String.class, 128, false);
        assertField(DictionaryTypeEntity.class, "securityRealm", String.class, 32, false);
        assertField(DictionaryTypeEntity.class, "status", String.class, 32, false);
        assertField(DictionaryTypeEntity.class, "sortOrder", Integer.class, 255, false);
        assertField(DictionaryTypeEntity.class, "builtIn", Boolean.class, 255, false);
    }

    @Test
    void dictionaryItemEntityContainsEntryFields() throws NoSuchFieldException {
        assertPersistenceId(DictionaryItemEntity.class.getDeclaredField("dictionaryItemId"));
        assertField(DictionaryItemEntity.class, "dictionaryItemId", String.class, 32, false);
        assertField(DictionaryItemEntity.class, "typeCode", String.class, 64, false);
        assertField(DictionaryItemEntity.class, "itemValue", String.class, 64, false);
        assertField(DictionaryItemEntity.class, "itemName", String.class, 128, false);
        assertField(DictionaryItemEntity.class, "securityRealm", String.class, 32, false);
        assertField(DictionaryItemEntity.class, "status", String.class, 32, false);
        assertField(DictionaryItemEntity.class, "sortOrder", Integer.class, 255, false);
        assertField(DictionaryItemEntity.class, "builtIn", Boolean.class, 255, false);
    }

    @Test
    void dictionaryEntitiesDeclareRealmAwareIndexes() {
        assertIndex(DictionaryTypeEntity.class, "uk_nx_dictionary_type_code",
                "workspace_id,security_realm,type_code", true);
        assertIndex(DictionaryTypeEntity.class, "idx_nx_dictionary_type_status",
                "workspace_id,status", false);
        assertIndex(DictionaryTypeEntity.class, "idx_nx_dictionary_type_realm",
                "security_realm", false);
        assertIndex(DictionaryItemEntity.class, "uk_nx_dictionary_item_value",
                "workspace_id,security_realm,type_code,item_value", true);
        assertIndex(DictionaryItemEntity.class, "idx_nx_dictionary_item_type",
                "workspace_id,type_code,sort_order", false);
        assertIndex(DictionaryItemEntity.class, "idx_nx_dictionary_item_realm",
                "security_realm", false);
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
