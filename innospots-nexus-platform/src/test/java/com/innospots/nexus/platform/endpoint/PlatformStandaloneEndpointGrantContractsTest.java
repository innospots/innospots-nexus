package com.innospots.nexus.platform.endpoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.platform.access.endpoint.PlatformAccessRequestEndpoint;
import com.innospots.nexus.platform.access.endpoint.PlatformPublicAccessRegistrationEndpoint;
import com.innospots.nexus.platform.access.endpoint.PlatformPublicOpenRegistrationEndpoint;
import com.innospots.nexus.platform.access.endpoint.PlatformPublicRegistrationPolicyEndpoint;
import com.innospots.nexus.platform.auth.endpoint.PlatformAuthSessionEndpoint;
import com.innospots.nexus.platform.auth.endpoint.PlatformPublicAuthEndpoint;
import com.innospots.nexus.platform.auth.endpoint.PlatformPublicPasswordResetEndpoint;
import com.innospots.nexus.platform.dictionary.endpoint.PlatformDictionaryTypeEndpoint;
import com.innospots.nexus.platform.invite.endpoint.PlatformInviteEndpoint;
import com.innospots.nexus.platform.invite.endpoint.PlatformPublicInviteEndpoint;
import com.innospots.nexus.platform.logger.endpoint.PlatformAuditLogEndpoint;
import com.innospots.nexus.platform.navigation.endpoint.PlatformNavigationMenuEndpoint;
import com.innospots.nexus.platform.organization.endpoint.EnterpriseProfileEndpoint;
import com.innospots.nexus.platform.organization.endpoint.TenantEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformCurrentAuthorizationEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformGrantManagementEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformOrganizationUnitGrantManagementEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformPageAccessEndpoint;
import com.innospots.nexus.platform.role.endpoint.PlatformRoleBindingEndpoint;
import com.innospots.nexus.platform.role.endpoint.PlatformRoleEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleDisplayLanguageSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleGeneralSettingsEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleSiteDescriptionSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleSiteNameSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleThemeSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleTimezoneSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformNavigationLayoutSettingEndpoint;
import com.innospots.nexus.platform.user.endpoint.PlatformAdminUserEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * platform 自有 REST 资源须在本类声明 {@link EndpointModule} 与 {@link EndpointGrant}；
 * 实现 console 契约的类在实现类声明 {@link EndpointModule}，{@link EndpointGrant} 仍继承接口。
 */
class PlatformStandaloneEndpointGrantContractsTest {

    private static final List<Class<?>> STANDALONE_PLATFORM_ENDPOINTS = List.of(
            TenantEndpoint.class,
            EnterpriseProfileEndpoint.class,
            PlatformAdminUserEndpoint.class,
            PlatformInviteEndpoint.class,
            PlatformPublicInviteEndpoint.class,
            PlatformAccessRequestEndpoint.class,
            PlatformPublicAccessRegistrationEndpoint.class,
            PlatformPublicOpenRegistrationEndpoint.class,
            PlatformPublicRegistrationPolicyEndpoint.class,
            PlatformConsoleGeneralSettingsEndpoint.class,
            PlatformConsoleSiteNameSettingEndpoint.class,
            PlatformConsoleSiteDescriptionSettingEndpoint.class,
            PlatformConsoleDisplayLanguageSettingEndpoint.class,
            PlatformConsoleTimezoneSettingEndpoint.class,
            PlatformConsoleThemeSettingEndpoint.class,
            PlatformNavigationLayoutSettingEndpoint.class);

    private static final List<Class<?>> CONSOLE_CONTRACT_IMPLEMENTATIONS = List.of(
            PlatformDictionaryTypeEndpoint.class,
            PlatformRoleEndpoint.class,
            PlatformRoleBindingEndpoint.class,
            PlatformNavigationMenuEndpoint.class,
            PlatformGrantManagementEndpoint.class,
            PlatformOrganizationUnitGrantManagementEndpoint.class,
            PlatformCurrentAuthorizationEndpoint.class,
            PlatformPageAccessEndpoint.class,
            PlatformAuditLogEndpoint.class,
            PlatformAuthSessionEndpoint.class,
            PlatformPublicAuthEndpoint.class,
            PlatformPublicPasswordResetEndpoint.class);

    @Test
    void standaloneEndpointsDeclareModuleAndMethodGrants() {
        for (Class<?> endpointType : STANDALONE_PLATFORM_ENDPOINTS) {
            assertThat(endpointType.getAnnotation(EndpointModule.class))
                    .as("%s", endpointType.getSimpleName())
                    .isNotNull();
            for (Method method : endpointType.getDeclaredMethods()) {
                if (!declaresHttpVerb(method)) {
                    continue;
                }
                assertThat(method.getAnnotation(EndpointGrant.class))
                        .as("%s#%s", endpointType.getSimpleName(), method.getName())
                        .isNotNull();
            }
        }
    }

    @Test
    void consoleContractImplementationsDeclareModuleOnClass() {
        for (Class<?> endpointType : CONSOLE_CONTRACT_IMPLEMENTATIONS) {
            assertThat(endpointType.getAnnotation(EndpointModule.class))
                    .as("%s must declare @EndpointModule on implementation", endpointType.getSimpleName())
                    .isNotNull();
            for (Method method : endpointType.getDeclaredMethods()) {
                if (!declaresHttpVerb(method)) {
                    continue;
                }
                assertThat(method.getAnnotation(EndpointGrant.class))
                        .as("%s#%s", endpointType.getSimpleName(), method.getName())
                        .isNull();
            }
        }
    }

    @Test
    void consoleContractInterfacesDeclareTransportAnnotations() {
        for (Class<?> endpointType : CONSOLE_CONTRACT_IMPLEMENTATIONS) {
            assertThat(endpointType.getAnnotation(Produces.class))
                    .as("%s must declare @Produces", endpointType.getSimpleName())
                    .isNotNull();
            for (Class<?> contract : endpointType.getInterfaces()) {
                if (!contract.getName().startsWith("com.innospots.nexus.console")) {
                    continue;
                }
                for (Method method : contract.getDeclaredMethods()) {
                    if (!declaresHttpVerb(method)) {
                        continue;
                    }
                    assertThat(method.getAnnotation(Operation.class))
                            .as("%s#%s must declare @Operation", contract.getSimpleName(), method.getName())
                            .isNotNull();
                }
            }
        }
    }

    @Test
    void consoleContractImplementationsDoNotRepeatInterfaceMethodAnnotations() throws NoSuchMethodException {
        for (Class<?> endpointType : CONSOLE_CONTRACT_IMPLEMENTATIONS) {
            for (Class<?> contract : endpointType.getInterfaces()) {
                if (!contract.getName().startsWith("com.innospots.nexus.console")) {
                    continue;
                }
                for (Method contractMethod : contract.getDeclaredMethods()) {
                    if (!declaresHttpVerb(contractMethod)) {
                        continue;
                    }
                    Method implMethod = endpointType.getMethod(
                            contractMethod.getName(),
                            contractMethod.getParameterTypes());
                    assertThat(implMethod.getAnnotations())
                            .as("%s#%s must only use @Override", endpointType.getSimpleName(), implMethod.getName())
                            .allMatch(annotation -> annotation.annotationType().equals(Override.class));
                }
            }
        }
    }

    @Test
    void standaloneEndpointSetIsComplete() {
        Set<String> expected = STANDALONE_PLATFORM_ENDPOINTS.stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toSet());
        Set<String> actual = Set.of(
                "TenantEndpoint",
                "EnterpriseProfileEndpoint",
                "PlatformAdminUserEndpoint",
                "PlatformInviteEndpoint",
                "PlatformPublicInviteEndpoint",
                "PlatformAccessRequestEndpoint",
                "PlatformPublicAccessRegistrationEndpoint",
                "PlatformPublicOpenRegistrationEndpoint",
                "PlatformPublicRegistrationPolicyEndpoint",
                "PlatformConsoleGeneralSettingsEndpoint",
                "PlatformConsoleSiteNameSettingEndpoint",
                "PlatformConsoleSiteDescriptionSettingEndpoint",
                "PlatformConsoleDisplayLanguageSettingEndpoint",
                "PlatformConsoleTimezoneSettingEndpoint",
                "PlatformConsoleThemeSettingEndpoint",
                "PlatformNavigationLayoutSettingEndpoint");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void consoleSettingUpdateGrantsUseUpdateCatalogGroup() {
        Map<Class<?>, String> updateMethods = Map.of(
                PlatformConsoleSiteNameSettingEndpoint.class, "updateSiteNameSetting",
                PlatformConsoleSiteDescriptionSettingEndpoint.class, "updateSiteDescriptionSetting",
                PlatformConsoleDisplayLanguageSettingEndpoint.class, "updateDisplayLanguageSetting",
                PlatformConsoleTimezoneSettingEndpoint.class, "updateTimezoneSetting",
                PlatformConsoleThemeSettingEndpoint.class, "updateConsoleThemeSetting",
                PlatformNavigationLayoutSettingEndpoint.class, "updateNavigationLayoutSetting");

        updateMethods.forEach((endpointType, methodName) -> {
            Method updateMethod = java.util.Arrays.stream(endpointType.getDeclaredMethods())
                    .filter(method -> method.getName().equals(methodName))
                    .findFirst()
                    .orElseThrow();
            EndpointGrant grant = updateMethod.getAnnotation(EndpointGrant.class);

            assertThat(grant)
                    .as("%s#%s", endpointType.getSimpleName(), methodName)
                    .isNotNull();
            assertThat(grant.action())
                    .as("%s#%s action", endpointType.getSimpleName(), methodName)
                    .isEqualTo(com.innospots.nexus.core.endpoint.grant.EndpointGrantAction.UPDATE);
            assertThat(grant.group())
                    .as("%s#%s group", endpointType.getSimpleName(), methodName)
                    .isEqualTo("update");
        });
    }

    private static boolean declaresHttpVerb(Method method) {
        return hasAnnotation(method, GET.class)
                || hasAnnotation(method, POST.class)
                || hasAnnotation(method, PUT.class)
                || hasAnnotation(method, DELETE.class);
    }

    private static boolean hasAnnotation(Method method, Class<? extends Annotation> annotationType) {
        return method.getAnnotation(annotationType) != null;
    }
}
