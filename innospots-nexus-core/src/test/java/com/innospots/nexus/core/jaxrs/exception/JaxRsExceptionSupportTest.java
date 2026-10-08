package com.innospots.nexus.core.jaxrs.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.jaxrs.support.HttpHeaderNames;

import static org.assertj.core.api.Assertions.assertThat;

class JaxRsExceptionSupportTest {

    private final JaxRsExceptionSupport support = new JaxRsExceptionSupport();

    @Test
    void mapsNexusExceptionToLegacyRWithHttpStatusAndRequestIdHeader() {
        NexusException exception = NexusException.build(NexusStatusCode.INVALID_PARAMETER);

        Response response = support.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.httpStatusCode());
        assertThat(response.getHeaderString(HttpHeaderNames.REQUEST_ID)).isEqualTo("unknown");
        assertThat(response.getEntity()).isInstanceOf(R.class);
    }

    @Test
    void keepsNativeWebApplicationResponseWithoutLegacyRWrapping() {
        WebApplicationException exception = new NotFoundException();

        Response response = support.toWebApplicationResponse(exception);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity()).isNull();
    }

    @Test
    void mapsUnknownThrowableToSystemError() {
        NexusException mapped = NexusException.build(NexusStatusCode.SYSTEM_ERROR, new IllegalStateException("boom"));

        Response response = support.toResponse(mapped);

        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
        assertThat(response.getEntity()).isInstanceOf(R.class);
    }
}
