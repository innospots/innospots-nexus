package com.innospots.nexus.platform.access.endpoint;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRegistrationSubmitRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformRegistrationOtpRequest;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPublicAccessRegistrationEndpointContractsTest {

    @Test
    void publicAccessRegistrationEndpointIsAnonymous() {
        assertThat(PlatformPublicAccessRegistrationEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.PUBLIC_ACCESS_REGISTRATION_PATH);
        assertThat(PlatformPublicAccessRegistrationEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformPublicAccessRegistration");
        assertThat(PlatformPublicAccessRegistrationEndpoint.class.getAnnotation(NexusAuthenticatedApi.class))
                .isNull();
    }

    @Test
    void publicAccessRegistrationExposesOtpAndSubmit() throws NoSuchMethodException {
        assertThat(PlatformPublicAccessRegistrationEndpoint.class.getMethod(
                        "issueVerificationOtp", PlatformRegistrationOtpRequest.class)
                .getAnnotation(POST.class)).isNotNull();
        assertThat(PlatformPublicAccessRegistrationEndpoint.class.getMethod(
                        "submitRegistration", PlatformAccessRegistrationSubmitRequest.class)
                .getAnnotation(POST.class)).isNotNull();
    }
}
