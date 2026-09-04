package com.innospots.nexus.console.catalog.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.enums.CatalogResourceType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsoleCatalogServiceTest {

    @Test
    void buildsEnabledResourceTree() {
        ConsoleCatalogResourceEntity module = resource("module-1", null, CatalogResourceType.MODULE,
                "module:sales", 0);
        ConsoleCatalogResourceEntity menu = resource("menu-1", "module-1", CatalogResourceType.MENU,
                "menu:sales.orders", 1);
        ConsoleCatalogService service = new ConsoleCatalogService(
                permissionResourceDao(List.of(module, menu)));

        List<CatalogNodeVo> tree = service.tree();

        assertThat(tree).hasSize(1);
        assertThat(tree.getFirst().resourceType()).isEqualTo(CatalogResourceType.MODULE);
        assertThat(tree.getFirst().children()).singleElement()
                .extracting(CatalogNodeVo::resourceType)
                .isEqualTo(CatalogResourceType.MENU);
    }

    private static com.innospots.nexus.core.plugin.contribution.console.catalog.dao.ConsoleCatalogResourceDao permissionResourceDao(
            List<ConsoleCatalogResourceEntity> resources
    ) {
        com.innospots.nexus.core.plugin.contribution.console.catalog.dao.ConsoleCatalogResourceDao dao =
                mock(com.innospots.nexus.core.plugin.contribution.console.catalog.dao.ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(resources);
        return dao;
    }

    private static ConsoleCatalogResourceEntity resource(
            String id,
            String parentId,
            CatalogResourceType type,
            String resourceKey,
            int sortOrder
    ) {
        ConsoleCatalogResourceEntity entity = new ConsoleCatalogResourceEntity();
        entity.setResourceId(id);
        entity.setParentResourceId(parentId);
        entity.setResourceType(type.name());
        entity.setResourceKey(resourceKey);
        entity.setSortOrder(sortOrder);
        entity.setStatus("ENABLED");
        entity.setOwnerPluginId("plugin-1");
        entity.setModuleKey("sales");
        entity.setDisplayName(resourceKey);
        return entity;
    }
}
