package com.innospots.nexus.console.catalog.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;

class ConsoleCatalogEntityContractsTest {

    @Test
    void tableNameIsStable() {
        assertThat(ConsoleCatalogResourceEntity.TABLE_NAME)
                .isEqualTo("nx_console_catalog_resource");
    }

    @Test
    void resourceTypesAreModuleAndMenuOnly() {
        assertThat(CatalogResourceType.values()).containsExactly(
                CatalogResourceType.MODULE,
                CatalogResourceType.MENU);
    }
}
