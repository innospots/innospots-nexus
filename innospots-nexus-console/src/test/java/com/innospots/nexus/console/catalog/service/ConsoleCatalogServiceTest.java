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
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;

class ConsoleCatalogServiceTest {

    @Test
    void permissionCatalogBuildsModuleViewGroupsFromReadOnlyGrants() {
        ConsoleCatalogResourceEntity module = entity(
                "m1", null, CatalogResourceType.MODULE, "module:dictionary", "dictionary", "platform-dictionary-main");
        module.setDomainKey("platform");
        module.setViewGroups(Jsons.toJson(List.of(Map.of(
                "group", "default",
                "label", Map.of("zh-CN", "查看", "en", "View")))));
        module.setOperationGroups(Jsons.toJson(List.of(Map.of(
                "group", "default",
                "label", Map.of("zh-CN", "操作", "en", "Operations")))));

        ConsoleCatalogResourceDao dao = mock(ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(List.of(module));

        EndpointGrantManager grantManager = new EndpointGrantManager();
        grantManager.registerResource(SampleDictionaryResource.class);

        ConsoleCatalogService service = new ConsoleCatalogService(dao, grantManager);

        List<CatalogNodeVo> roots = service.permissionCatalog();
        assertThat(roots).hasSize(1);
        assertThat(roots.getFirst().grants()).isNotNull();
        assertThat(roots.getFirst().grants().viewGroups()).hasSize(1);
        assertThat(roots.getFirst().grants().viewGroups().getFirst().items())
                .extracting(item -> item.grantName())
                .contains("dictionary.type.page", "dictionary.type.get")
                .doesNotContain("dictionary.type.create");
        assertThat(roots.getFirst().grants().viewGroups().getFirst().items())
                .extracting(item -> item.resourceKey())
                .contains("dictionary.type.page:platform.dictionary");
        assertThat(roots.getFirst().grants().operationGroups().getFirst().items())
                .extracting(item -> item.grantName())
                .contains("dictionary.type.create")
                .doesNotContain("dictionary.type.page");
    }

    @Test
    void permissionCatalogBuildsEmptyViewGroupsWhenModuleHasNoEndpointCatalog() {
        ConsoleCatalogResourceEntity module = entity(
                "m1", null, CatalogResourceType.MODULE, "module:demo", "demo", "demo-main");
        module.setViewGroups(Jsons.toJson(List.of(Map.of(
                "group", "default",
                "label", Map.of("en", "View")))));
        module.setOperationGroups(Jsons.toJson(List.of(Map.of(
                "group", "default",
                "label", Map.of("en", "Operations")))));

        ConsoleCatalogResourceDao dao = mock(ConsoleCatalogResourceDao.class);
        when(dao.selectList(any())).thenReturn(List.of(module));

        ConsoleCatalogService service = new ConsoleCatalogService(dao, new EndpointGrantManager());

        List<CatalogNodeVo> roots = service.permissionCatalog();
        assertThat(roots).hasSize(1);
        assertThat(roots.getFirst().grants().viewGroups()).hasSize(1);
        assertThat(roots.getFirst().grants().viewGroups().getFirst().items()).isEmpty();
        assertThat(roots.getFirst().grants().operationGroups().getFirst().items()).isEmpty();
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
