package com.innospots.nexus.core.endpoint.grant.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;

class EndpointGrantAnnotationsTest {

    @Test
    void resolvesGrantDeclaredOnInterfaceMethod() throws NoSuchMethodException {
        Method method = DictionaryTypeEndpoint.class.getMethod(
                "createDictionaryType",
                DictionaryTypeCreateRequest.class);

        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, null);

        assertThat(grant).isNotNull();
        assertThat(grant.moduleKey()).isEqualTo("dictionary");
        assertThat(grant.action()).isEqualTo(EndpointGrantAction.CREATE);
        assertThat(grant.name()).isEqualTo("dictionary.type.create");
        assertThat(grant.auditLog()).isTrue();
        assertThat(grant.recordArgs()).isFalse();
    }

    @Test
    void resolveModuleOnContractInterface() {
        assertThat(EndpointGrantAnnotations.resolveModule(DictionaryTypeEndpoint.class).value())
                .isEqualTo("dictionary");
        assertThat(EndpointGrantAnnotations.isGrantResourceType(DictionaryTypeEndpoint.class)).isTrue();
    }

    @Test
    void mergesModuleIgnoreFlags() throws NoSuchMethodException {
        Method method = DictionaryTypeEndpoint.class.getMethod(
                "listDictionaryTypeOptions",
                com.innospots.nexus.base.domain.enums.BasicStatus.class);

        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, null);

        assertThat(grant).isNotNull();
        assertThat(grant.ignorePermission()).isTrue();
        assertThat(grant.auditLog()).isFalse();
    }

    @Test
    void resolveReturnsNullWhenMethodMissing(TestReporter reporter) {
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(null, GrantOnImpl.class);
        reporter.publishEntry("input", "implementationMethod=null");
        reporter.publishEntry("output", String.valueOf(grant));
        assertThat(grant).isNull();
    }

    @Test
    void resolveReturnsNullWhenNoGrantAnnotation(TestReporter reporter) throws NoSuchMethodException {
        Method method = NoGrantResource.class.getMethod("plain");
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, NoGrantResource.class);
        reporter.publishEntry("input", "method=plain, resourceClass=NoGrantResource");
        reporter.publishEntry("output", String.valueOf(grant));
        assertThat(grant).isNull();
    }

    @Test
    void resolveModuleReturnsNullForNullClass(TestReporter reporter) {
        reporter.publishEntry("input", "resourceClass=null");
        reporter.publishEntry("output", "module=" + EndpointGrantAnnotations.resolveModule(null));
        assertThat(EndpointGrantAnnotations.resolveModule(null)).isNull();
        assertThat(EndpointGrantAnnotations.isGrantResourceType(null)).isFalse();
    }

    @Test
    void resolveModulePrefersDirectClassAnnotation(TestReporter reporter) {
        EndpointModule module = EndpointGrantAnnotations.resolveModule(ModuleOnClassResource.class);
        reporter.publishEntry("input", "resourceClass=ModuleOnClassResource");
        reporter.publishEntry("output", "moduleKey=" + module.value());
        assertThat(module.value()).isEqualTo("class-module");
        assertThat(EndpointGrantAnnotations.isGrantResourceType(ModuleOnClassResource.class)).isTrue();
    }

    @Test
    void resolveModuleFallsBackToImplementedInterface(TestReporter reporter) {
        EndpointModule module = EndpointGrantAnnotations.resolveModule(GrantOnImpl.class);
        reporter.publishEntry("input", "resourceClass=GrantOnImpl");
        reporter.publishEntry("output", "moduleKey=" + module.value());
        assertThat(module.value()).isEqualTo("impl-module");
    }

    @Test
    void resolvePrefersGrantOnImplementationMethod(TestReporter reporter) throws NoSuchMethodException {
        Method implMethod = GrantOnImpl.class.getMethod("action");
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(implMethod, GrantOnImpl.class);
        reporter.publishEntry("input", "implementationMethod=GrantOnImpl#action");
        reporter.publishEntry("output", grant == null ? "null" : grant.toString());
        assertThat(grant).isNotNull();
        assertThat(grant.moduleKey()).isEqualTo("impl-module");
        assertThat(grant.name()).isEqualTo("impl.action");
        assertThat(grant.action()).isEqualTo(EndpointGrantAction.UPDATE);
    }

    @Test
    void resolveReadsGrantFromContractWhenMissingOnImpl(TestReporter reporter) throws NoSuchMethodException {
        Method implMethod = GrantOnlyOnContractImpl.class.getMethod("fromContract");
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(implMethod, GrantOnlyOnContractImpl.class);
        reporter.publishEntry("input", "implementationMethod=GrantOnlyOnContractImpl#fromContract");
        reporter.publishEntry("output", grant == null ? "null" : grant.toString());
        assertThat(grant).isNotNull();
        assertThat(grant.name()).isEqualTo("contract.only");
        assertThat(grant.moduleKey()).isEqualTo("contract-module");
    }

    @Test
    void mergeCombinesModuleAndMethodIgnoreAuthFlags(TestReporter reporter) throws NoSuchMethodException {
        Method method = ModuleIgnoreAuthContract.class.getMethod("secured");
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, null);
        reporter.publishEntry("input", "interface method with module ignoreAuth + method ignorePermission");
        reporter.publishEntry("output", grant == null ? "null" : grant.toString());
        assertThat(grant).isNotNull();
        assertThat(grant.ignoreAuth()).isTrue();
        assertThat(grant.ignorePermission()).isTrue();
    }

    @Test
    void resolveUsesEmptyModuleKeyWhenGrantWithoutModule(TestReporter reporter) throws NoSuchMethodException {
        Method method = GrantWithoutModule.class.getMethod("solo");
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, GrantWithoutModule.class);
        reporter.publishEntry("input", "GrantWithoutModule#solo");
        reporter.publishEntry("output", grant == null ? "null" : grant.toString());
        assertThat(grant).isNotNull();
        assertThat(grant.moduleKey()).isEmpty();
        assertThat(grant.name()).isEqualTo("solo");
    }

    @EndpointModule("impl-module")
    interface GrantOnContract {

        @EndpointGrant(
                action = EndpointGrantAction.READ,
                name = "contract.fallback",
                auditLog = false)
        void action();
    }

    static final class GrantOnImpl implements GrantOnContract {

        @Override
        @EndpointGrant(
                action = EndpointGrantAction.UPDATE,
                name = "impl.action",
                label = @I18nLiteral(pairs = {"zh-CN", "实现", "en", "Impl"}),
                auditLog = true)
        public void action() {
        }
    }

    @EndpointModule(value = "contract-module", ignorePermission = true)
    interface GrantOnlyOnContract {

        @EndpointGrant(
                action = EndpointGrantAction.READ,
                name = "contract.only",
                auditLog = false)
        void fromContract();
    }

    static final class GrantOnlyOnContractImpl implements GrantOnlyOnContract {

        @Override
        public void fromContract() {
        }
    }

    @EndpointModule(value = "auth-module", ignoreAuth = true)
    interface ModuleIgnoreAuthContract {

        @EndpointGrant(
                action = EndpointGrantAction.READ,
                name = "secured",
                ignorePermission = true,
                auditLog = false)
        void secured();
    }

    @EndpointModule("class-module")
    static final class ModuleOnClassResource {

        public void noop() {
        }
    }

    static final class NoGrantResource {

        public void plain() {
        }
    }

    static final class GrantWithoutModule {

        @EndpointGrant(
                action = EndpointGrantAction.READ,
                name = "solo",
                auditLog = false)
        public void solo() {
        }
    }
}
