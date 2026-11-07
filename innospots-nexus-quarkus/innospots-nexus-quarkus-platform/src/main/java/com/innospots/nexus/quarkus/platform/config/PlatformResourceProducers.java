package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import io.quarkus.arc.Unremovable;

import com.innospots.nexus.platform.auth.endpoint.PlatformAuthSessionEndpoint;
import com.innospots.nexus.platform.auth.endpoint.PlatformPublicAuthEndpoint;
import com.innospots.nexus.platform.auth.endpoint.PlatformPublicPasswordResetEndpoint;
import com.innospots.nexus.platform.auth.service.PlatformAuthService;
import com.innospots.nexus.platform.auth.service.PlatformPasswordService;
import com.innospots.nexus.platform.access.endpoint.PlatformAccessRequestEndpoint;
import com.innospots.nexus.platform.access.endpoint.PlatformPublicAccessRegistrationEndpoint;
import com.innospots.nexus.platform.access.endpoint.PlatformPublicOpenRegistrationEndpoint;
import com.innospots.nexus.platform.access.endpoint.PlatformPublicRegistrationPolicyEndpoint;
import com.innospots.nexus.console.auth.service.RegistrationPolicyService;
import com.innospots.nexus.platform.access.service.PlatformAccessRequestService;
import com.innospots.nexus.platform.access.service.PlatformOpenRegistrationService;
import com.innospots.nexus.platform.invite.endpoint.PlatformInviteEndpoint;
import com.innospots.nexus.platform.invite.endpoint.PlatformPublicInviteEndpoint;
import com.innospots.nexus.platform.invite.service.PlatformInviteService;
import com.innospots.nexus.platform.invite.service.PlatformPublicInviteService;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleDisplayLanguageSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleGeneralSettingsEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleSiteDescriptionSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleSiteNameSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleThemeSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleTimezoneSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformNavigationLayoutSettingEndpoint;
import com.innospots.nexus.platform.settings.service.PlatformConsoleGeneralSettingService;
import com.innospots.nexus.platform.settings.service.PlatformConsoleThemeSettingService;
import com.innospots.nexus.platform.settings.service.PlatformNavigationLayoutSettingService;
import com.innospots.nexus.platform.organization.endpoint.TenantEndpoint;
import com.innospots.nexus.platform.organization.endpoint.EnterpriseProfileEndpoint;
import com.innospots.nexus.platform.organization.service.PlatformEnterpriseProfileService;
import com.innospots.nexus.platform.organization.service.PlatformTenantService;
import com.innospots.nexus.console.user.admin.endpoint.AdminUserEndpoint;
import com.innospots.nexus.platform.user.endpoint.PlatformAdminUserEndpoint;
import com.innospots.nexus.console.user.admin.service.AdminUserService;

/**
 * 运营管理平台 REST 资源生产者。
 */
@ApplicationScoped
public class PlatformResourceProducers {

    private final PlatformAuthService platformAuthService;
    private final PlatformPasswordService platformPasswordService;
    private final AdminUserService platformUserService;
    private final PlatformInviteService platformInviteService;
    private final PlatformPublicInviteService platformPublicInviteService;
    private final PlatformAccessRequestService platformAccessRequestService;
    private final PlatformOpenRegistrationService platformOpenRegistrationService;
    private final PlatformConsoleGeneralSettingService platformConsoleGeneralSettingService;
    private final PlatformConsoleThemeSettingService platformConsoleThemeSettingService;
    private final PlatformNavigationLayoutSettingService platformNavigationLayoutSettingService;
    private final RegistrationPolicyService registrationPolicyService;
    private final PlatformTenantService platformTenantService;
    private final PlatformEnterpriseProfileService platformEnterpriseProfileService;

    @Inject
    public PlatformResourceProducers(
            PlatformAuthService platformAuthService,
            PlatformPasswordService platformPasswordService,
            AdminUserService platformUserService,
            PlatformInviteService platformInviteService,
            PlatformPublicInviteService platformPublicInviteService,
            PlatformAccessRequestService platformAccessRequestService,
            PlatformOpenRegistrationService platformOpenRegistrationService,
            PlatformConsoleGeneralSettingService platformConsoleGeneralSettingService,
            PlatformConsoleThemeSettingService platformConsoleThemeSettingService,
            PlatformNavigationLayoutSettingService platformNavigationLayoutSettingService,
            RegistrationPolicyService registrationPolicyService,
            PlatformTenantService platformTenantService,
            PlatformEnterpriseProfileService platformEnterpriseProfileService) {
        this.platformAuthService = platformAuthService;
        this.platformPasswordService = platformPasswordService;
        this.platformUserService = platformUserService;
        this.platformInviteService = platformInviteService;
        this.platformPublicInviteService = platformPublicInviteService;
        this.platformAccessRequestService = platformAccessRequestService;
        this.platformOpenRegistrationService = platformOpenRegistrationService;
        this.platformConsoleGeneralSettingService = platformConsoleGeneralSettingService;
        this.platformConsoleThemeSettingService = platformConsoleThemeSettingService;
        this.platformNavigationLayoutSettingService = platformNavigationLayoutSettingService;
        this.registrationPolicyService = registrationPolicyService;
        this.platformTenantService = platformTenantService;
        this.platformEnterpriseProfileService = platformEnterpriseProfileService;
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformPublicAuthEndpoint platformPublicAuthEndpoint() {
        return new PlatformPublicAuthEndpoint(platformAuthService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformPublicPasswordResetEndpoint platformPublicPasswordResetEndpoint() {
        return new PlatformPublicPasswordResetEndpoint(platformPasswordService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformAuthSessionEndpoint platformAuthSessionEndpoint() {
        return new PlatformAuthSessionEndpoint(platformAuthService, platformPasswordService);
    }

    @Produces
    @Dependent
    @Unremovable
    AdminUserEndpoint platformUserEndpoint() {
        return new PlatformAdminUserEndpoint(platformUserService);
    }

    @Produces
    @Dependent
    @Unremovable
    TenantEndpoint tenantEndpoint() {
        return new TenantEndpoint(platformTenantService);
    }

    @Produces
    @Dependent
    @Unremovable
    EnterpriseProfileEndpoint enterpriseProfileEndpoint() {
        return new EnterpriseProfileEndpoint(platformEnterpriseProfileService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformInviteEndpoint platformInviteEndpoint() {
        return new PlatformInviteEndpoint(platformInviteService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformPublicInviteEndpoint platformPublicInviteEndpoint() {
        return new PlatformPublicInviteEndpoint(platformPublicInviteService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformAccessRequestEndpoint platformAccessRequestEndpoint() {
        return new PlatformAccessRequestEndpoint(platformAccessRequestService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformPublicAccessRegistrationEndpoint platformPublicAccessRegistrationEndpoint() {
        return new PlatformPublicAccessRegistrationEndpoint(platformAccessRequestService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformPublicOpenRegistrationEndpoint platformPublicOpenRegistrationEndpoint() {
        return new PlatformPublicOpenRegistrationEndpoint(platformOpenRegistrationService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformPublicRegistrationPolicyEndpoint platformPublicRegistrationPolicyEndpoint() {
        return new PlatformPublicRegistrationPolicyEndpoint(registrationPolicyService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformConsoleGeneralSettingsEndpoint platformConsoleGeneralSettingsEndpoint() {
        return new PlatformConsoleGeneralSettingsEndpoint(platformConsoleGeneralSettingService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformConsoleSiteNameSettingEndpoint platformConsoleSiteNameSettingEndpoint() {
        return new PlatformConsoleSiteNameSettingEndpoint(platformConsoleGeneralSettingService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformConsoleSiteDescriptionSettingEndpoint platformConsoleSiteDescriptionSettingEndpoint() {
        return new PlatformConsoleSiteDescriptionSettingEndpoint(platformConsoleGeneralSettingService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformConsoleDisplayLanguageSettingEndpoint platformConsoleDisplayLanguageSettingEndpoint() {
        return new PlatformConsoleDisplayLanguageSettingEndpoint(platformConsoleGeneralSettingService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformConsoleTimezoneSettingEndpoint platformConsoleTimezoneSettingEndpoint() {
        return new PlatformConsoleTimezoneSettingEndpoint(platformConsoleGeneralSettingService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformConsoleThemeSettingEndpoint platformConsoleThemeSettingEndpoint() {
        return new PlatformConsoleThemeSettingEndpoint(platformConsoleThemeSettingService);
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformNavigationLayoutSettingEndpoint platformNavigationLayoutSettingEndpoint() {
        return new PlatformNavigationLayoutSettingEndpoint(platformNavigationLayoutSettingService);
    }
}
