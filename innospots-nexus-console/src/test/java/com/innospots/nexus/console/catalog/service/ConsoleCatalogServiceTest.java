package com.innospots.nexus.console.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.vo.CatalogDomainVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.console.entry.ConsolePageRoutes;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;

class ConsoleCatalogServiceTest {

    @Test
    void permissionCatalogBuildsModuleViewGroupsFromDeclarations() {
        ConsoleCatalogResourceEntity module = entity(
                "m1", null, CatalogResourceType.MODULE, "module:dictionary", "dictionary", "platform-dictionary-main");
        module.setDomainKey("platform");
        module.setViewGroups(Jsons.toJson(List.of(Map.of(
                "group", "view",
                "label", Map.of("zh-CN", "字典管理", "en", "Dictionary Management")))));
        module.setOperationGroups(Jsons.toJson(List.of(
                Map.of("group", "operate", "label", Map.of("en", "Operations", "zh", "操作")),
                Map.of("group", "create", "label", Map.of("en", "Create", "zh", "创建")),
                Map.of("group", "update", "label", Map.of("en", "Update", "zh", "更新")),
                Map.of("group", "delete", "label", Map.of("en", "Delete", "zh", "删除")),
                Map.of("group", "status", "label", Map.of("en", "Status", "zh", "状态")))));

        ConsoleCatalogResourceDao dao = mock(ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(List.of(module));

        EndpointGrantManager grantManager = new EndpointGrantManager();
        grantManager.registerResource(SampleDictionaryResource.class);

        ConsoleCatalogService service = new ConsoleCatalogService(dao, grantManager);

        List<CatalogDomainVo> roots = service.permissionCatalog();
        assertThat(roots).hasSize(1);
        CatalogNodeVo moduleNode = roots.getFirst().modules().getFirst();
        assertThat(moduleNode.grants()).isNotNull();
        assertThat(moduleNode.grants().viewGroups()).hasSize(1);
        assertThat(moduleNode.grants().viewGroups().getFirst().group()).isEqualTo("view");
        assertThat(moduleNode.grants().viewGroups().getFirst().resourceKey())
                .isEqualTo("platform.dictionary:view");
        assertThat(moduleNode.grants().viewGroups().getFirst().items()).isEmpty();
        assertThat(moduleNode.grants().operationGroups().stream()
                .filter(group -> "create".equals(group.group()))
                .flatMap(group -> group.items().stream())
                .map(item -> item.grantName()))
                .contains("dictionary.type.create")
                .doesNotContain("dictionary.type.page");
    }

    @Test
    void permissionCatalogBuildsEmptyViewGroupsWhenModuleHasNoEndpointCatalog() {
        ConsoleCatalogResourceEntity module = entity(
                "m1", null, CatalogResourceType.MODULE, "module:demo", "demo", "demo-main");
        module.setViewGroups(Jsons.toJson(List.of(Map.of(
                "group", "view",
                "label", Map.of("en", "View")))));
        module.setOperationGroups(Jsons.toJson(List.of(Map.of(
                "group", "operate",
                "label", Map.of("en", "Operations")))));

        ConsoleCatalogResourceDao dao = mock(ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(List.of(module));

        ConsoleCatalogService service = new ConsoleCatalogService(dao, new EndpointGrantManager());

        List<CatalogDomainVo> roots = service.permissionCatalog();
        assertThat(roots).hasSize(1);
        CatalogNodeVo moduleNode = roots.getFirst().modules().getFirst();
        assertThat(moduleNode.grants().viewGroups()).hasSize(1);
        assertThat(moduleNode.grants().viewGroups().getFirst().resourceKey())
                .isEqualTo("nexus.demo:view");
        assertThat(moduleNode.grants().viewGroups().getFirst().items()).isEmpty();
        assertThat(moduleNode.grants().operationGroups().getFirst().items()).isEmpty();
    }

    @Test
    void permissionCatalogGroupsMultipleModulesUnderSameContribution() {
        ConsoleCatalogResourceEntity first = entity(
                "m1", null, CatalogResourceType.MODULE, "module:alpha", "alpha", "alpha-main");
        ConsoleCatalogResourceEntity second = entity(
                "m2", null, CatalogResourceType.MODULE, "module:beta", "beta", "beta-main");
        first.setSortOrder(10);
        second.setSortOrder(20);

        ConsoleCatalogResourceDao dao = mock(ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(List.of(first, second));

        ConsoleCatalogService service = new ConsoleCatalogService(dao, new EndpointGrantManager());

        List<CatalogDomainVo> roots = service.permissionCatalog();
        assertThat(roots).hasSize(1);
        assertThat(roots.getFirst().domainKey()).isEqualTo("nexus");
        assertThat(roots.getFirst().ownerPluginId()).isEqualTo("com.innospots.nexus.test-plugin");
        assertThat(roots.getFirst().modules()).extracting(CatalogNodeVo::moduleKey)
                .containsExactly("alpha", "beta");
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
        return entity;
    }

    @EndpointModule(value = "dictionary", domain = ConsolePageRoutes.BUILTIN_DOMAIN_KEY)
    @Path("/api/test/dictionary/types")
    static final class SampleDictionaryResource implements DictionaryTypeEndpoint {

        @Override
        public R<PageResult<DictionaryTypeVo>> pageDictionaryTypes(DictionaryTypePageRequest request) {
            return R.ok();
        }

        @Override
        public R<DictionaryTypeVo> getDictionaryType(@PathParam("dictionaryTypeId") String dictionaryTypeId) {
            return R.ok();
        }

        @Override
        public R<DictionaryTypeVo> createDictionaryType(DictionaryTypeCreateRequest request) {
            return R.ok();
        }

        @Override
        public R<DictionaryTypeVo> updateDictionaryType(
                String dictionaryTypeId,
                DictionaryTypeUpdateRequest request) {
            return R.ok();
        }

        @Override
        public R<Void> updateDictionaryTypeStatus(
                String dictionaryTypeId,
                DictionaryTypeStatusUpdateRequest request) {
            return R.ok();
        }

        @Override
        public R<Void> deleteDictionaryType(String dictionaryTypeId) {
            return R.ok();
        }

        @Override
        public R<List<DictionaryTypeOptionVo>> listDictionaryTypeOptions(BasicStatus status) {
            return R.ok();
        }
    }
}
