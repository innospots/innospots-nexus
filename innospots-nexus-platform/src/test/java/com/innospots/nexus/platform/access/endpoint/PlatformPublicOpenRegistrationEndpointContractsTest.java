package com.innospots.nexus.platform.access.endpoint;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.access.domain.request.PlatformOpenRegistrationSubmitRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformRegistrationOtpRequest;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPublicOpenRegistrationEndpointContractsTest {

    @Test
    void publicOpenRegistrationEndpointIsAnonymous() {
        assertThat(PlatformPublicOpenRegistrationEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.PUBLIC_OPEN_REGISTRATION_PATH);
        assertThat(PlatformPublicOpenRegistrationEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformPublicOpenRegistration");
        assertThat(PlatformPublicOpenRegistrationEndpoint.class.getAnnotation(NexusAuthenticatedApi.class))
                .isNull();
    }

    @Test
    void publicOpenRegistrationExposesOtpAndRegister() throws NoSuchMethodException {
        assertThat(PlatformPublicOpenRegistrationEndpoint.class.getMethod(
                        "issueVerificationOtp", PlatformRegistrationOtpRequest.class)
                .getAnnotation(POST.class)).isNotNull();
        assertThat(PlatformPublicOpenRegistrationEndpoint.class.getMethod(
                        "register", PlatformOpenRegistrationSubmitRequest.class)
                .getAnnotation(POST.class)).isNotNull();
    }
}
