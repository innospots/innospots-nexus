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

/**
 * platform 运维域 REST 资源生产者。
 */
@ApplicationScoped
public class PlatformResourceProducers {

    private final AuthFacade platformAuthFacade;
    private final PlatformPasswordOperator platformPasswordOperator;
    private final PasswordDecryptor passwordDecryptor;

    @Inject
    public PlatformResourceProducers(
            @Named("platformAuthFacade") AuthFacade platformAuthFacade,
            PlatformPasswordOperator platformPasswordOperator,
            PasswordDecryptor passwordDecryptor) {
        this.platformAuthFacade = platformAuthFacade;
        this.platformPasswordOperator = platformPasswordOperator;
        this.passwordDecryptor = passwordDecryptor;
    }

    @Produces
    @Dependent
    @Unremovable
    PlatformAuthEndpoint platformAuthEndpoint() {
        return new PlatformAuthEndpoint(platformAuthFacade, platformPasswordOperator, passwordDecryptor);
    }
}
