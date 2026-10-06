package com.innospots.nexus.platform.invite.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.invite.domain.request.PlatformInvitePageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformInviteEndpointContractsTest {

    @Test
    void inviteEndpointIsAuthenticatedAdminApi() {
        assertThat(PlatformInviteEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.INVITES_PATH);
        assertThat(PlatformInviteEndpoint.class.getAnnotation(Tag.class).name()).isEqualTo("PlatformInvite");
        assertThat(PlatformInviteEndpoint.class.getAnnotation(NexusAuthenticatedApi.class)).isNotNull();
        assertThat(PlatformInviteEndpoint.class.isInterface()).isFalse();
    }

    @Test
    void inviteEndpointExposesCrudAndResend() throws NoSuchMethodException {
        assertThat(PlatformInviteEndpoint.class.getMethod("pageInvites", PlatformInvitePageRequest.class)
                .getAnnotation(GET.class)).isNotNull();
        assertThat(PlatformInviteEndpoint.class.getMethod("createInvite", com.innospots.nexus.platform.invite.domain.request.PlatformInviteCreateRequest.class)
                .getAnnotation(POST.class)).isNotNull();
        assertThat(PlatformInviteEndpoint.class.getMethod("resendInvite", String.class, String.class)
                .getAnnotation(POST.class)).isNotNull();
    }
}
