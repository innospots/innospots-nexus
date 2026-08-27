package com.innospots.nexus.kernel.permission.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.ui.spec.PageInfo;
import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.action.ActionType;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.datasource.UiDatasource;
import com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider;
import com.innospots.nexus.core.extension.declaration.ExtensionDescriptor;
import com.innospots.nexus.core.extension.declaration.ExtensionModuleDeclaration;
import com.innospots.nexus.core.extension.declaration.MenuDeclaration;
import com.innospots.nexus.core.extension.declaration.UiSpecPageDeclaration;
import com.innospots.nexus.console.extension.service.ExtensionRegistry;
import com.innospots.nexus.console.permission.dao.PermissionResourceDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionResourceEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionResourceType;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceSyncVo;
import com.innospots.nexus.base.ui.spec.loader.UiSpecLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionResourceSyncServiceTest {

    @AfterEach
    void clearProjectContext() {
        TLC.clear();
    }

    @Test
    void buildsCatalogueFromActiveExtensionAndUiSpecAndIsIdempotent() {
        TLC.workspaceId("100");
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(new SalesExtension());
        registry.activate("com.example.sales");

        UiSpec spec = UiSpec.page(PageInfo.of("orders", I18nObject.of("en", "Orders")))
                .datasource("list", UiDatasource.get("/api/orders"))
                .datasource("approve", UiDatasource.post("/api/orders/{orderId}/approve"))
                .actionDefinition(UiAction.of("approve", ActionType.API)
                        .datasourceKey("approve"));
        UiSpecLoader loader = (moduleKey, pageKey) -> spec;
        PermissionResourceDao resourceDao = mock(PermissionResourceDao.class);
        List<PermissionResourceEntity> inserted = new ArrayList<>();
        doAnswer(invocation -> {
            PermissionResourceEntity entity = invocation.getArgument(0);
            entity.setResourceId("resource-" + inserted.size());
            inserted.add(entity);
            return 1;
        }).when(resourceDao).insert(any(PermissionResourceEntity.class));
        when(resourceDao.selectList(any())).thenReturn(List.of());

        PermissionResourceSyncService service = new PermissionResourceSyncService(
                resourceDao, registry, loader);

        PermissionResourceSyncVo first = service.sync();

        assertThat(first).isEqualTo(new PermissionResourceSyncVo(6, 0, 0));
        assertThat(inserted).extracting(PermissionResourceEntity::getResourceType)
                .containsExactlyInAnyOrder(
                        PermissionResourceType.MODULE.name(),
                        PermissionResourceType.MENU.name(),
                        PermissionResourceType.PAGE.name(),
                        PermissionResourceType.ACTION.name(),
                        PermissionResourceType.DATASOURCE.name(),
                        PermissionResourceType.DATASOURCE.name());
        assertThat(inserted).filteredOn(value ->
                        PermissionResourceType.DATASOURCE.name().equals(value.getResourceType()))
                .extracting(PermissionResourceEntity::getRequestMethod)
                .containsExactlyInAnyOrder("GET", "POST");

        when(resourceDao.selectList(any())).thenReturn(List.copyOf(inserted));
        PermissionResourceSyncVo second = service.sync();

        assertThat(second).isEqualTo(new PermissionResourceSyncVo(0, 0, 0));
    }

    @Test
    void updatesChangedResourceMetadata() {
        TLC.workspaceId("100");
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(new SalesExtension());
        registry.activate("com.example.sales");

        UiSpec firstSpec = UiSpec.page(PageInfo.of("orders", I18nObject.of("en", "Orders")))
                .datasource("list", UiDatasource.get("/api/orders"));
        UiSpec secondSpec = UiSpec.page(PageInfo.of("orders", I18nObject.of("en", "Orders")))
                .datasource("list", UiDatasource.post("/api/orders/search"));
        UiSpec[] current = {firstSpec};
        UiSpecLoader loader = (moduleKey, pageKey) -> current[0];
        PermissionResourceDao resourceDao = mock(PermissionResourceDao.class);
        List<PermissionResourceEntity> stored = new ArrayList<>();
        doAnswer(invocation -> {
            PermissionResourceEntity entity = invocation.getArgument(0);
            entity.setResourceId("resource-" + stored.size());
            stored.add(entity);
            return 1;
        }).when(resourceDao).insert(any(PermissionResourceEntity.class));
        when(resourceDao.selectList(any())).thenAnswer(invocation -> List.copyOf(stored));

        PermissionResourceSyncService service = new PermissionResourceSyncService(
                resourceDao, registry, loader);
        service.sync();
        current[0] = secondSpec;

        PermissionResourceSyncVo result = service.sync();

        assertThat(result).isEqualTo(new PermissionResourceSyncVo(0, 1, 0));
        assertThat(stored).filteredOn(value ->
                        PermissionResourceType.DATASOURCE.name().equals(value.getResourceType()))
                .singleElement()
                .satisfies(value -> {
                    assertThat(value.getRequestMethod()).isEqualTo("POST");
                    assertThat(value.getRequestUrl()).isEqualTo("/api/orders/search");
                });
        verify(resourceDao).updateById(any(PermissionResourceEntity.class));
    }

    private static ExtensionDescriptor descriptor() {
        return new ExtensionDescriptor(
                "com.example.sales",
                "1.0.0",
                I18nObject.of("en", "Sales"),
                I18nObject.of("en", "Sales extension"),
                List.of(new ExtensionModuleDeclaration(
                        "sales",
                        I18nObject.of("en", "Sales"),
                        I18nObject.of("en", "Sales module"),
                        List.of(new UiSpecPageDeclaration("orders", "/orders", List.of())),
                        List.of(MenuDeclaration.page(
                                "orders",
                                I18nObject.of("en", "Orders"),
                                null,
                                0,
                                "orders")))));
    }

    private static final class SalesExtension implements ConsoleExtensionProvider {

        @Override
        public ExtensionDescriptor descriptor() {
            return PermissionResourceSyncServiceTest.descriptor();
        }

        @Override
        public Collection<Class<?>> endpointTypes() {
            return List.of();
        }
    }
}
