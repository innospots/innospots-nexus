package com.innospots.nexus.console.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;

class ConsoleCatalogServiceTest {

    @Test
    void permissionCatalogBuildsModuleWithGrants() {
        ConsoleCatalogResourceEntity module = entity(
                "m1", null, CatalogResourceType.MODULE, "module:demo", "demo", null);
        ConsoleCatalogResourceEntity menu = entity(
                "u1", "m1", CatalogResourceType.MENU, "menu:demo.demo-main", "demo", "demo-main");

        ConsoleCatalogResourceDao dao = mock(ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(List.of(module, menu));

        ConsoleCatalogService service = new ConsoleCatalogService(
                dao,
                mock(ConsoleContributionCatalog.class),
                new EndpointGrantManager());

        List<CatalogNodeVo> roots = service.permissionCatalog();
        assertThat(roots).hasSize(1);
        assertThat(roots.getFirst().resourceType()).isEqualTo(CatalogResourceType.MODULE);
        assertThat(roots.getFirst().grants()).isNotNull();
        assertThat(roots.getFirst().grants().viewItems()).hasSize(1);
    }

    private static ConsoleCatalogResourceEntity entity(
            String id,
            String parentId,
            CatalogResourceType type,
            String resourceKey,
            String moduleKey,
            String entryPageKey
    ) {
        ConsoleCatalogResourceEntity entity = new ConsoleCatalogResourceEntity();
        entity.setResourceId(id);
        entity.setParentResourceId(parentId);
        entity.setOwnerPluginId("com.innospots.nexus.test-plugin");
        entity.setDomainKey("nexus");
        entity.setModuleKey(moduleKey);
        entity.setResourceType(type.name());
        entity.setResourceKey(resourceKey);
        entity.setEntryPageKey(entryPageKey);
        entity.setDisplayName(Jsons.toJson(I18nObject.of(Map.of("en", "label"))));
        entity.setSortOrder(0);
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setSecurityRealm("TENANT");
        if (type == CatalogResourceType.MENU) {
            entity.setViewGroups("[]");
            entity.setOperationGroups("[]");
        }
        return entity;
    }
}
