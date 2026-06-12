package com.innospots.nexus.kernel.menu.domain.entity;

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

class MenuEntityContractsTest {

    @Test
    void menuEntityExposesProjectScopedPersistenceTable() {
        assertThat(MenuEntity.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(MenuEntity.class.getAnnotation(Table.class).name()).isEqualTo("nx_menu");
        assertThat(MenuEntity.class.getAnnotation(TableName.class).value()).isEqualTo("nx_menu");
        assertThat(MenuEntity.class.getSuperclass()).isEqualTo(ProjectBaseEntity.class);
    }

    @Test
    void menuEntityContainsNavigationAndLifecycleFields() throws NoSuchFieldException {
        assertPersistenceId(MenuEntity.class.getDeclaredField("menuId"));
        assertField("menuId", String.class, 32, false);
        assertField("parentId", String.class, 32, true);
        assertField("menuKey", String.class, 64, false);
        assertField("menuName", String.class, 128, false);
        assertField("menuType", String.class, 32, false);
        assertField("routePath", String.class, 256, true);
        assertField("componentKey", String.class, 128, true);
        assertField("redirectPath", String.class, 256, true);
        assertField("externalUrl", String.class, 512, true);
        assertField("icon", String.class, 128, true);
        assertField("openMode", String.class, 32, false);
        assertField("visible", Boolean.class, 255, false);
        assertField("status", String.class, 32, false);
        assertField("sortOrder", Integer.class, 255, false);
        assertField("builtIn", Boolean.class, 255, false);
    }

    @Test
    void menuEntityDeclaresTreeAndManagementIndexes() {
        assertIndex("uk_nx_menu_project_key", "project_id,menu_key", true);
        assertIndex("idx_nx_menu_project_parent_order", "project_id,parent_id,sort_order", false);
        assertIndex("idx_nx_menu_project_status_visible", "project_id,status,visible", false);
    }

    private static void assertPersistenceId(Field field) {
        assertThat(field.getAnnotation(Id.class)).isNotNull();
        assertThat(field.getAnnotation(TableId.class).type()).isEqualTo(IdType.ASSIGN_UUID);
    }

    private static void assertField(String fieldName, Class<?> fieldType, int length, boolean nullable)
            throws NoSuchFieldException {
        Field field = MenuEntity.class.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);

        assertThat(field.getType()).isEqualTo(fieldType);
        assertThat(column).isNotNull();
        assertThat(column.length()).isEqualTo(length);
        assertThat(column.nullable()).isEqualTo(nullable);
    }

    private static void assertIndex(String indexName, String columnList, boolean unique) {
        assertThat(MenuEntity.class.getAnnotation(Table.class).indexes())
                .filteredOn(index -> index.name().equals(indexName))
                .singleElement()
                .satisfies(index -> {
                    Index tableIndex = (Index) index;
                    assertThat(tableIndex.columnList()).isEqualTo(columnList);
                    assertThat(tableIndex.unique()).isEqualTo(unique);
                });
    }
}
