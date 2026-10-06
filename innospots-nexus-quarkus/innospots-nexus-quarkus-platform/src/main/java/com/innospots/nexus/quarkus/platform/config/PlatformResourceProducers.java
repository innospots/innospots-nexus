package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import io.quarkus.arc.Unremovable;

import com.innospots.nexus.console.auth.service.AuthFacade;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.auth.endpoint.PlatformAuthEndpoint;
import com.innospots.nexus.platform.auth.operator.PlatformPasswordOperator;
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
import com.innospots.nexus.platform.user.endpoint.PlatformUserEndpoint;
import com.innospots.nexus.platform.user.service.PlatformUserService;

/**
 * 运营管理平台 REST 资源生产者。
 */
@ApplicationScoped
public class PlatformResourceProducers {

    private final AuthFacade platformAuthFacade;
    private final PlatformPasswordOperator platformPasswordOperator;
    private final PasswordDecryptor passwordDecryptor;
    private final PlatformUserService platformUserService;
    private final PlatformInviteService platformInviteService;
    private final PlatformPublicInviteService platformPublicInviteService;
    private final PlatformAccessRequestService platformAccessRequestService;
    private final PlatformOpenRegistrationService platformOpenRegistrationService;
    private final PlatformRegistrationModeSettingService platformRegistrationModeSettingService;

    @Inject
    public PlatformResourceProducers(
            @Named("platformAuthFacade") AuthFacade platformAuthFacade,
            PlatformPasswordOperator platformPasswordOperator,
            PasswordDecryptor passwordDecryptor,
            PlatformUserService platformUserService,
            PlatformInviteService platformInviteService,
            PlatformPublicInviteService platformPublicInviteService,
            PlatformAccessRequestService platformAccessRequestService,
            PlatformOpenRegistrationService platformOpenRegistrationService,
            PlatformRegistrationModeSettingService platformRegistrationModeSettingService) {
        this.platformAuthFacade = platformAuthFacade;
        this.platformPasswordOperator = platformPasswordOperator;
        this.passwordDecryptor = passwordDecryptor;
        this.platformUserService = platformUserService;
        this.platformInviteService = platformInviteService;
        this.platformPublicInviteService = platformPublicInviteService;
        this.platformAccessRequestService = platformAccessRequestService;
        this.platformOpenRegistrationService = platformOpenRegistrationService;
        this.platformRegistrationModeSettingService = platformRegistrationModeSettingService;
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformAuthEndpoint platformAuthEndpoint() {
        return new PlatformAuthEndpoint(platformAuthFacade, platformPasswordOperator, passwordDecryptor);
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
