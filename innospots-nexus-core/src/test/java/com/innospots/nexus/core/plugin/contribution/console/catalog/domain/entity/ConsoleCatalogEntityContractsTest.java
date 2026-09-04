package com.innospots.nexus.core.plugin.contribution.console.catalog.domain.entity;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.enums.CatalogResourceType;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleCatalogEntityContractsTest {

    @Test
    void catalogEntityIsHostGlobalAndUsesDedicatedTable() {
        assertThat(ConsoleCatalogResourceEntity.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(ConsoleCatalogResourceEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("nx_console_catalog_resource");
        assertThat(Arrays.stream(ConsoleCatalogResourceEntity.class.getDeclaredFields())
                .map(Field::getName))
                .doesNotContain("workspaceId", "targetKey", "resourceDefinition", "permissionId");
    }

    @Test
    void catalogEntityIsolatesBySecurityRealm() throws NoSuchFieldException {
        Field field = ConsoleCatalogResourceEntity.class.getDeclaredField("securityRealm");
        Column column = field.getAnnotation(Column.class);
        assertThat(field.getType()).isEqualTo(String.class);
        assertThat(column).isNotNull();
        assertThat(column.length()).isEqualTo(32);
        assertThat(column.nullable()).isFalse();
        assertThat(CatalogResourceType.values()).containsExactly(
                CatalogResourceType.MODULE,
                CatalogResourceType.MENU,
                CatalogResourceType.PAGE,
                CatalogResourceType.ACTION,
                CatalogResourceType.DATASOURCE,
                CatalogResourceType.CAPABILITY);
    }
}
