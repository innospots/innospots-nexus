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
import com.innospots.nexus.platform.access.service.PlatformAccessRequestService;
import com.innospots.nexus.platform.access.service.PlatformOpenRegistrationService;
import com.innospots.nexus.platform.invite.endpoint.PlatformInviteEndpoint;
import com.innospots.nexus.platform.invite.endpoint.PlatformPublicInviteEndpoint;
import com.innospots.nexus.platform.invite.service.PlatformInviteService;
import com.innospots.nexus.platform.invite.service.PlatformPublicInviteService;
import com.innospots.nexus.platform.settings.endpoint.PlatformRegistrationModeSettingEndpoint;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;
import com.innospots.nexus.platform.organization.endpoint.TenantEndpoint;
import com.innospots.nexus.platform.organization.endpoint.EnterpriseProfileEndpoint;
import com.innospots.nexus.platform.organization.service.PlatformEnterpriseProfileService;
import com.innospots.nexus.platform.organization.service.PlatformTenantService;
import com.innospots.nexus.platform.user.endpoint.PlatformUserEndpoint;
import com.innospots.nexus.platform.user.service.PlatformUserService;

/**
 * 运营管理平台 REST 资源生产者。
 */
@ApplicationScoped
public class PlatformResourceProducers {

    private final PlatformAuthService platformAuthService;
    private final PlatformPasswordService platformPasswordService;
    private final PlatformUserService platformUserService;
    private final PlatformInviteService platformInviteService;
    private final PlatformPublicInviteService platformPublicInviteService;
    private final PlatformAccessRequestService platformAccessRequestService;
    private final PlatformOpenRegistrationService platformOpenRegistrationService;
    private final PlatformRegistrationModeSettingService platformRegistrationModeSettingService;
    private final PlatformTenantService platformTenantService;
    private final PlatformEnterpriseProfileService platformEnterpriseProfileService;

    @Inject
    public PlatformResourceProducers(
            PlatformAuthService platformAuthService,
            PlatformPasswordService platformPasswordService,
            PlatformUserService platformUserService,
            PlatformInviteService platformInviteService,
            PlatformPublicInviteService platformPublicInviteService,
            PlatformAccessRequestService platformAccessRequestService,
            PlatformOpenRegistrationService platformOpenRegistrationService,
            PlatformRegistrationModeSettingService platformRegistrationModeSettingService,
            PlatformTenantService platformTenantService,
            PlatformEnterpriseProfileService platformEnterpriseProfileService) {
        this.platformAuthService = platformAuthService;
        this.platformPasswordService = platformPasswordService;
        this.platformUserService = platformUserService;
        this.platformInviteService = platformInviteService;
        this.platformPublicInviteService = platformPublicInviteService;
        this.platformAccessRequestService = platformAccessRequestService;
        this.platformOpenRegistrationService = platformOpenRegistrationService;
        this.platformRegistrationModeSettingService = platformRegistrationModeSettingService;
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
    PlatformUserEndpoint platformUserEndpoint() {
        return new PlatformUserEndpoint(platformUserService);
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
    PlatformRegistrationModeSettingEndpoint platformRegistrationModeSettingEndpoint() {
        return new PlatformRegistrationModeSettingEndpoint(platformRegistrationModeSettingService);
    }
}
