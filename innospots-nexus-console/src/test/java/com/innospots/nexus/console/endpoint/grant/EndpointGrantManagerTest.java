package com.innospots.nexus.console.endpoint.grant;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.console.endpoint.grant.support.EndpointGrantAnnotations;
import com.innospots.nexus.console.endpoint.grant.support.EndpointGrantHttpIntrospection;
import com.innospots.nexus.console.endpoint.grant.support.EndpointGrantPathMatcher;
class EndpointGrantManagerTest {

    @Test
    void grantResourceTypeResolvedFromImplementedContract() {
        assertThat(EndpointGrantAnnotations.isGrantResourceType(SampleDictionaryResource.class)).isTrue();
        assertThat(EndpointGrantAnnotations.resolveModule(SampleDictionaryResource.class).value())
                .isEqualTo("dictionary");
    }

    @Test
    void registersModuleCatalogAndMatchesPath() {
        EndpointGrantManager manager = new EndpointGrantManager();
        manager.registerResource(SampleDictionaryResource.class);

        EndpointModuleGrantCatalogVo catalog = manager.moduleCatalog("dictionary").orElseThrow();
        assertThat(catalog.readOnly()).hasSize(3);
        assertThat(catalog.operable()).isNotEmpty();
        assertThat(catalog.readOnly())
                .extracting(EndpointGrantDescriptorVo::name)
                .contains("dictionary.type.page", "dictionary.type.get");

        EndpointGrantDescriptorVo page = catalog.readOnly().stream()
                .filter(item -> "dictionary.type.page".equals(item.name()))
                .findFirst()
                .orElseThrow();
        assertThat(page.httpMethod()).isEqualTo("GET");
        assertThat(page.path()).isEqualTo("/api/test/dictionary/types");
        assertThat(page.label().cnValue()).isEqualTo("分页查询字典类型");
        assertThat(page.ignoreAuth()).isFalse();

        assertThat(manager.match("GET", "/api/test/dictionary/types")).isPresent();
        assertThat(manager.match("GET", "/api/test/dictionary/types/dt-1")).isPresent();
        assertThat(manager.match("POST", "/api/test/dictionary/types")).isEmpty();
    }

    @Test
    void pathMatcherSupportsJaxRsTemplates() {
        assertThat(EndpointGrantPathMatcher.matches(
                "/api/test/dictionary/types/{dictionaryTypeId}",
                "/api/test/dictionary/types/abc"))
                .isTrue();
        assertThat(EndpointGrantPathMatcher.matches(
                "/api/test/dictionary/types/{dictionaryTypeId}",
                "/api/test/dictionary/types/abc/extra"))
                .isFalse();
    }

    @Test
    void httpIntrospectionJoinsClassAndInterfacePaths() throws NoSuchMethodException {
        var method = SampleDictionaryResource.class.getMethod(
                "getDictionaryType",
                String.class);
        var routes = EndpointGrantHttpIntrospection.resolveRoutes(SampleDictionaryResource.class, method);
        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).httpMethod()).isEqualTo("GET");
        assertThat(routes.get(0).pathTemplate())
                .isEqualTo("/api/test/dictionary/types/{dictionaryTypeId}");
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
        public R<DictionaryTypeVo> createDictionaryType(
                com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest request) {
            return R.ok();
        }

        @Override
        public R<DictionaryTypeVo> updateDictionaryType(
                String dictionaryTypeId,
                com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest request) {
            return R.ok();
        }

        @Override
        public R<Void> updateDictionaryTypeStatus(
                String dictionaryTypeId,
                com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest request) {
            return R.ok();
        }

        @Override
        public R<Void> deleteDictionaryType(String dictionaryTypeId) {
            return R.ok();
        }

        @Override
        public R<java.util.List<com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo>>
                listDictionaryTypeOptions(BasicStatus status) {
            return R.ok();
        }
    }
}
