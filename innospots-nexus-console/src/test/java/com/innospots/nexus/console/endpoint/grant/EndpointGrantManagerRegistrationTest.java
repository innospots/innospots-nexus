package com.innospots.nexus.console.endpoint.grant;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.console.logger.I18nLiteral;

/**
 * {@link EndpointGrantManager} 注册、目录聚合与路由匹配的专项测试。
 */
class EndpointGrantManagerRegistrationTest {

    private EndpointGrantManager manager;

    @BeforeEach
    void setUp() {
        manager = new EndpointGrantManager();
    }

    @Test
    void registerResourceIgnoresNull(TestReporter reporter) {
        manager.registerResource(null);

        reporter.publishEntry("input", "resourceClass=null");
        reporter.publishEntry("output", "allModuleCatalogs=" + manager.allModuleCatalogs());

        assertThat(manager.allModuleCatalogs()).isEmpty();
        assertThat(manager.moduleCatalog("any")).isEmpty();
    }

    @Test
    void registerResourceSkipsTypesWithoutModuleOrHttpRoute(TestReporter reporter) {
        manager.registerResource(GrantWithoutModuleResource.class);
        manager.registerResource(GrantWithoutHttpVerbResource.class);
        manager.registerResource(PlainResource.class);

        reporter.publishEntry("input", "GrantWithoutModule, GrantWithoutHttpVerb, PlainResource");
        reporter.publishEntry("output", "allModuleCatalogs=" + manager.allModuleCatalogs());

        assertThat(manager.allModuleCatalogs()).isEmpty();
    }

    @Test
    void registersCatalogFixtureWithCorrectBuckets(TestReporter reporter) {
        manager.registerResource(CatalogFixtureResource.class);

        EndpointModuleGrantCatalogVo catalog = manager.moduleCatalog("catalog-fixture").orElseThrow();
        reporter.publishEntry("input", "resourceClass=CatalogFixtureResource");
        reporter.publishEntry("output", catalog.toString());

        assertThat(catalog.moduleKey()).isEqualTo("catalog-fixture");
        assertThat(catalog.readOnly()).extracting(EndpointGrantDescriptorVo::name)
                .containsExactlyInAnyOrder("fixture.read", "fixture.export", "fixture.public");
        assertThat(catalog.operable()).extracting(EndpointGrantDescriptorVo::name)
                .contains("fixture.create", "fixture.delete", "fixture.execute", "fixture.upsert");
        assertThat(catalog.operable().stream().filter(d -> "fixture.upsert".equals(d.name())).count())
                .isEqualTo(2);

        EndpointGrantDescriptorVo create = findDescriptor(catalog, "fixture.create");
        assertThat(create.httpMethod()).isEqualTo("POST");
        assertThat(create.path()).isEqualTo("/api/fixture/create");
        assertThat(create.action()).isEqualTo(EndpointGrantAction.CREATE);
        assertThat(create.auditLog()).isTrue();
        assertThat(create.recordArgs()).isTrue();
        assertThat(create.label().cnValue()).isEqualTo("创建");
        assertThat(create.resourceClass()).isEqualTo(CatalogFixtureResource.class.getName());
        assertThat(create.resourceMethod()).isEqualTo("create");

        EndpointGrantDescriptorVo export = findDescriptor(catalog, "fixture.export");
        assertThat(export.action()).isEqualTo(EndpointGrantAction.EXPORT);
        assertThat(catalog.readOnly()).contains(export);

        EndpointGrantDescriptorVo execute = findDescriptor(catalog, "fixture.execute");
        assertThat(execute.action()).isEqualTo(EndpointGrantAction.EXECUTE);
        assertThat(catalog.operable()).contains(execute);
        assertThat(catalog.ignorePermission()).contains(execute);
        assertThat(catalog.ignoreAuth()).doesNotContain(execute);

        EndpointGrantDescriptorVo publicRead = findDescriptor(catalog, "fixture.public");
        assertThat(catalog.ignoreAuth()).contains(publicRead);
        assertThat(catalog.ignorePermission()).contains(publicRead);
    }

    @Test
    void registersContractImplementationFromConsoleInterface(TestReporter reporter) {
        manager.registerResource(ContractOnlyImpl.class);

        EndpointModuleGrantCatalogVo catalog = manager.moduleCatalog("contract-only").orElseThrow();
        EndpointGrantDescriptorVo list = findDescriptor(catalog, "contract.list");

        reporter.publishEntry("input", "resourceClass=ContractOnlyImpl");
        reporter.publishEntry("output", list.toString());

        assertThat(list.httpMethod()).isEqualTo("GET");
        assertThat(list.path()).isEqualTo("/impl/items");
        assertThat(list.action()).isEqualTo(EndpointGrantAction.READ);
        assertThat(list.resourceClass()).isEqualTo(ContractOnlyImpl.class.getName());
        assertThat(list.resourceMethod()).isEqualTo("list");
    }

    @Test
    void registersDictionaryContractImplementation(TestReporter reporter) {
        manager.registerResource(DictionaryResourceFixture.class);

        EndpointModuleGrantCatalogVo catalog = manager.moduleCatalog("dictionary").orElseThrow();
        reporter.publishEntry("input", "resourceClass=DictionaryResourceFixture");
        reporter.publishEntry("output", "readOnly=" + catalog.readOnly().size()
                + ", operable=" + catalog.operable().size());

        assertThat(catalog.readOnly()).hasSize(3);
        assertThat(catalog.operable()).isNotEmpty();
        assertThat(catalog.ignorePermission()).extracting(EndpointGrantDescriptorVo::name)
                .contains("dictionary.type.options");

        EndpointGrantDescriptorVo page = findDescriptor(catalog, "dictionary.type.page");
        assertThat(page.httpMethod()).isEqualTo("GET");
        assertThat(page.path()).isEqualTo("/api/test/dictionary/types");
        assertThat(page.label().cnValue()).isEqualTo("分页查询字典类型");
    }

    @Test
    void allModuleCatalogsHoldsMultipleModules(TestReporter reporter) {
        manager.registerResource(CatalogFixtureResource.class);
        manager.registerResource(OtherModuleResource.class);

        Map<String, EndpointModuleGrantCatalogVo> all = manager.allModuleCatalogs();
        reporter.publishEntry("input", "CatalogFixtureResource + OtherModuleResource");
        reporter.publishEntry("output", "moduleKeys=" + all.keySet());

        assertThat(all).containsOnlyKeys("catalog-fixture", "other-module");
        assertThat(all.get("other-module").readOnly()).extracting(EndpointGrantDescriptorVo::name)
                .containsExactly("other.ping");
    }

    @Test
    void moduleCatalogRejectsBlankModuleKey(TestReporter reporter) {
        manager.registerResource(CatalogFixtureResource.class);

        reporter.publishEntry("input", "moduleKey=null | blank");
        reporter.publishEntry("output", "present=" + manager.moduleCatalog("catalog-fixture").isPresent());

        assertThat(manager.moduleCatalog(null)).isEmpty();
        assertThat(manager.moduleCatalog("  ")).isEmpty();
        assertThat(manager.moduleCatalog("missing")).isEmpty();
    }

    @Test
    void matchResolvesRegisteredRoutes(TestReporter reporter) {
        manager.registerResource(CatalogFixtureResource.class);

        var getRead = manager.match("get", "/api/fixture/read");
        var postCreate = manager.match("POST", "/api/fixture/create");
        var wrongVerb = manager.match("GET", "/api/fixture/create");
        var wrongPath = manager.match("GET", "/api/fixture/unknown");

        reporter.publishEntry("match", "GET /api/fixture/read -> " + getRead.map(EndpointGrantDescriptorVo::name));
        reporter.publishEntry("match", "POST /api/fixture/create -> " + postCreate.map(EndpointGrantDescriptorVo::name));
        reporter.publishEntry("match", "GET /api/fixture/create -> empty=" + wrongVerb.isEmpty());

        assertThat(getRead).map(EndpointGrantDescriptorVo::name).hasValue("fixture.read");
        assertThat(postCreate).map(EndpointGrantDescriptorVo::name).hasValue("fixture.create");
        assertThat(wrongVerb).isEmpty();
        assertThat(wrongPath).isEmpty();
        assertThat(manager.match(null, "/api/fixture/read")).isEmpty();
        assertThat(manager.match("GET", null)).isEmpty();
    }

    @Test
    void matchSupportsJaxRsPathParameters(TestReporter reporter) {
        manager.registerResource(DictionaryResourceFixture.class);

        var byId = manager.match("GET", "/api/test/dictionary/types/dt-99");
        reporter.publishEntry("input", "GET /api/test/dictionary/types/dt-99");
        reporter.publishEntry("output", byId.map(EndpointGrantDescriptorVo::name).orElse(""));

        assertThat(byId).map(EndpointGrantDescriptorVo::name).hasValue("dictionary.type.get");
    }

    @Test
    void resolveFromResourceInfo(TestReporter reporter) throws NoSuchMethodException {
        manager.registerResource(CatalogFixtureResource.class);

        Method method = CatalogFixtureResource.class.getMethod("read");
        ResourceInfo resourceInfo = new FixedResourceInfo(method, CatalogFixtureResource.class);

        EndpointGrantDescriptorVo descriptor = manager.resolve(resourceInfo).orElseThrow();
        reporter.publishEntry("input", "ResourceInfo -> CatalogFixtureResource#read");
        reporter.publishEntry("output", descriptor.toString());

        assertThat(descriptor.name()).isEqualTo("fixture.read");
        assertThat(descriptor.path()).isEqualTo("/api/fixture/read");
        assertThat(descriptor.httpMethod()).isEqualTo("GET");
    }

    @Test
    void resolveReturnsEmptyWhenResourceInfoIncomplete(TestReporter reporter) {
        ResourceInfo missingMethod = new FixedResourceInfo(null, CatalogFixtureResource.class);

        reporter.publishEntry("input", "resourceMethod=null");
        reporter.publishEntry("output", "resolve empty=" + manager.resolve(null).isEmpty());

        assertThat(manager.resolve(null)).isEmpty();
        assertThat(manager.resolve(missingMethod)).isEmpty();
    }

    private record FixedResourceInfo(Method resourceMethod, Class<?> resourceClass) implements ResourceInfo {

        @Override
        public Method getResourceMethod() {
            return resourceMethod;
        }

        @Override
        public Class<?> getResourceClass() {
            return resourceClass;
        }
    }

    @Test
    void repeatedRegistrationAccumulatesDuplicateEntries(TestReporter reporter) {
        manager.registerResource(OtherModuleResource.class);
        manager.registerResource(OtherModuleResource.class);

        EndpointModuleGrantCatalogVo catalog = manager.moduleCatalog("other-module").orElseThrow();
        reporter.publishEntry("input", "register OtherModuleResource twice");
        reporter.publishEntry("output", "readOnly size=" + catalog.readOnly().size());

        assertThat(catalog.readOnly()).hasSize(2);
        assertThat(manager.match("GET", "/api/other/ping")).isPresent();
    }

    private static EndpointGrantDescriptorVo findDescriptor(
            EndpointModuleGrantCatalogVo catalog,
            String name
    ) {
        Set<EndpointGrantDescriptorVo> merged = catalog.readOnly().stream()
                .collect(Collectors.toSet());
        catalog.operable().forEach(merged::add);
        return merged.stream()
                .filter(item -> name.equals(item.name()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing descriptor: " + name));
    }

    @EndpointModule("catalog-fixture")
    @Path("/api/fixture")
    static final class CatalogFixtureResource {

        @GET
        @Path("/read")
        @EndpointGrant(action = EndpointGrantAction.READ, name = "fixture.read", auditLog = false)
        public void read() {
        }

        @GET
        @Path("/export")
        @EndpointGrant(action = EndpointGrantAction.EXPORT, name = "fixture.export", auditLog = false)
        public void export() {
        }

        @POST
        @Path("/create")
        @EndpointGrant(
                action = EndpointGrantAction.CREATE,
                name = "fixture.create",
                label = @I18nLiteral(pairs = {"zh-CN", "创建", "en", "Create"}),
                recordArgs = true,
                auditLog = true)
        public void create() {
        }

        @DELETE
        @Path("/remove")
        @EndpointGrant(action = EndpointGrantAction.DELETE, name = "fixture.delete", auditLog = false)
        public void delete() {
        }

        @POST
        @Path("/execute")
        @EndpointGrant(
                action = EndpointGrantAction.EXECUTE,
                name = "fixture.execute",
                ignorePermission = true,
                auditLog = false)
        public void execute() {
        }

        @GET
        @Path("/public")
        @EndpointGrant(
                action = EndpointGrantAction.READ,
                name = "fixture.public",
                ignoreAuth = true,
                ignorePermission = true,
                auditLog = false)
        public void publicRead() {
        }

        @POST
        @PUT
        @Path("/upsert")
        @EndpointGrant(action = EndpointGrantAction.UPDATE, name = "fixture.upsert", auditLog = false)
        public void upsert() {
        }

        @EndpointGrant(action = EndpointGrantAction.READ, name = "fixture.silent", auditLog = false)
        public void silentWithoutHttpVerb() {
        }
    }

    static final class GrantWithoutModuleResource {

        @GET
        @Path("/solo")
        @EndpointGrant(action = EndpointGrantAction.READ, name = "solo", auditLog = false)
        public void solo() {
        }
    }

    @EndpointModule("noop")
    static final class GrantWithoutHttpVerbResource {

        @EndpointGrant(action = EndpointGrantAction.READ, name = "no.http", auditLog = false)
        public void noHttp() {
        }
    }

    @Path("/plain")
    static final class PlainResource {

        @GET
        public void ping() {
        }
    }

    @EndpointModule("contract-only")
    interface ContractOnlyEndpoint {

        @GET
        @Path("/items")
        @EndpointGrant(action = EndpointGrantAction.READ, name = "contract.list", auditLog = false)
        String list();
    }

    @Path("/impl")
    static final class ContractOnlyImpl implements ContractOnlyEndpoint {

        @Override
        public String list() {
            return "ok";
        }
    }

    @EndpointModule("other-module")
    @Path("/api/other")
    static final class OtherModuleResource {

        @GET
        @Path("/ping")
        @EndpointGrant(action = EndpointGrantAction.READ, name = "other.ping", auditLog = false)
        public void ping() {
        }
    }

    @Path("/api/test/dictionary/types")
    static final class DictionaryResourceFixture implements DictionaryTypeEndpoint {

        @Override
        public R<PageResult<DictionaryTypeVo>> pageDictionaryTypes(DictionaryTypePageRequest request) {
            return R.ok();
        }

        @Override
        public R<DictionaryTypeVo> getDictionaryType(String dictionaryTypeId) {
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
        public R<java.util.List<DictionaryTypeOptionVo>> listDictionaryTypeOptions(BasicStatus status) {
            return R.ok();
        }
    }
}
