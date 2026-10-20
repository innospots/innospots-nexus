package com.innospots.nexus.console.catalog.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeOperationVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantDescriptorVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.console.endpoint.grant.EndpointModuleGrantCatalogVo;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsoleCatalogServiceTest {

    @Test
    void buildsModuleTreeWithPageChildrenOnly() {
        ConsoleCatalogResourceEntity module = resource("module-1", null, CatalogResourceType.MODULE,
                "module:sales", 0, null, null);
        ConsoleCatalogResourceEntity menu = resource("menu-1", "module-1", CatalogResourceType.MENU,
                "menu:sales.orders", 1, null, null);
        ConsoleCatalogResourceEntity page = resource("page-1", "module-1", CatalogResourceType.PAGE,
                "page:sales.orders", 2, "orders", "/page/nexus/sales/orders");
        ConsoleCatalogService service = new ConsoleCatalogService(
                permissionResourceDao(List.of(module, menu, page)),
                grantManagerWithSalesOperations());

        List<CatalogNodeVo> tree = service.tree();

        assertThat(tree).hasSize(1);
        assertThat(tree.getFirst().resourceType()).isEqualTo(CatalogResourceType.MODULE);
        assertThat(tree.getFirst().domainKey()).isEqualTo("nexus");
        assertThat(tree.getFirst().operations()).singleElement()
                .satisfies(op -> {
                    assertThat(op.name()).isEqualTo("sales.order.create");
                    assertThat(op.action()).isEqualTo(EndpointGrantAction.CREATE);
                    assertThat(op.path()).isEqualTo("/api/d/nexus/sales/orders");
                });
        assertThat(tree.getFirst().children()).singleElement()
                .satisfies(child -> {
                    assertThat(child.resourceType()).isEqualTo(CatalogResourceType.PAGE);
                    assertThat(child.pageKey()).isEqualTo("orders");
                    assertThat(child.routePath()).isEqualTo("/page/nexus/sales/orders");
                    assertThat(child.operations()).hasSize(1);
                    assertThat(child.children()).isEmpty();
                });
    }

    @Test
    void buildsNestedPageTreeUnderModule() {
        ConsoleCatalogResourceEntity module = resource("module-1", null, CatalogResourceType.MODULE,
                "module:sales", 0, null, null);
        ConsoleCatalogResourceEntity parentPage = resource("page-1", "module-1", CatalogResourceType.PAGE,
                "page:sales.orders", 0, "orders", "/page/nexus/sales/orders");
        ConsoleCatalogResourceEntity childPage = resource("page-2", "page-1", CatalogResourceType.PAGE,
                "page:sales.order-detail", 1, "order-detail", "/page/nexus/sales/order-detail");
        ConsoleCatalogService service = new ConsoleCatalogService(
                permissionResourceDao(List.of(module, parentPage, childPage)),
                grantManagerWithSalesOperations());

        List<CatalogNodeVo> tree = service.tree();

        assertThat(tree.getFirst().children()).singleElement()
                .satisfies(parent -> {
                    assertThat(parent.pageKey()).isEqualTo("orders");
                    assertThat(parent.children()).singleElement()
                            .extracting(CatalogNodeVo::pageKey)
                            .isEqualTo("order-detail");
                });
    }

    @Test
    void parsesDomainKeyFromRoutePath() {
        assertThat(ConsoleCatalogService.domainKeyFromRoute("/page/nexus/menu/menu-main")).isEqualTo("nexus");
    }

    private static EndpointGrantManager grantManagerWithSalesOperations() {
        EndpointGrantManager manager = mock(EndpointGrantManager.class);
        I18nObject label = new I18nObject();
        label.put("zh-CN", "分页");
        EndpointGrantDescriptorVo descriptor = new EndpointGrantDescriptorVo(
                "sales",
                "POST",
                "/api/d/nexus/sales/orders",
                "sales.order.create",
                label,
                EndpointGrantAction.CREATE,
                false,
                true,
                false,
                false,
                "com.example.SalesEndpoint",
                "createOrder");
        EndpointModuleGrantCatalogVo catalog = new EndpointModuleGrantCatalogVo(
                "sales",
                List.of(),
                List.of(descriptor),
                List.of(),
                List.of());
        when(manager.allModuleCatalogs()).thenReturn(Map.of("sales", catalog));
        return manager;
    }

    private static com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao permissionResourceDao(
            List<ConsoleCatalogResourceEntity> resources
    ) {
        com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao dao =
                mock(com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(resources);
        return dao;
    }

    private static ConsoleCatalogResourceEntity resource(
            String id,
            String parentId,
            CatalogResourceType type,
            String resourceKey,
            int sortOrder,
            String pageKey,
            String routePath
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
        entity.setPageKey(pageKey);
        entity.setRoutePath(routePath);
        return entity;
    }
}
