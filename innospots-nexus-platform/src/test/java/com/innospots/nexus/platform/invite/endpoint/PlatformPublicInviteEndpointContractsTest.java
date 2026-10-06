package com.innospots.nexus.platform.invite.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteAcceptRequest;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteActivateByCodeRequest;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteOtpRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPublicInviteEndpointContractsTest {

    @Test
    void publicInviteEndpointIsAnonymous() {
        assertThat(PlatformPublicInviteEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.PUBLIC_INVITES_PATH);
        assertThat(PlatformPublicInviteEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformPublicInvite");
        assertThat(PlatformPublicInviteEndpoint.class.getAnnotation(NexusAuthenticatedApi.class)).isNull();
    }

    @Test
    void publicInviteEndpointExposesPreviewAcceptAndCodeActivation() throws NoSuchMethodException {
        assertThat(PlatformPublicInviteEndpoint.class.getMethod("previewInvite", String.class)
                .getAnnotation(GET.class)).isNotNull();
        assertThat(PlatformPublicInviteEndpoint.class.getMethod(
                        "issueInviteAcceptOtp", String.class, PlatformInviteOtpRequest.class)
                .getAnnotation(POST.class)).isNotNull();
        assertThat(PlatformPublicInviteEndpoint.class.getMethod(
                        "acceptInvite", String.class, PlatformInviteAcceptRequest.class)
                .getAnnotation(POST.class)).isNotNull();
        assertThat(PlatformPublicInviteEndpoint.class.getMethod(
                        "activateByInviteCode", PlatformInviteActivateByCodeRequest.class)
                .getAnnotation(POST.class)).isNotNull();
    }
}
