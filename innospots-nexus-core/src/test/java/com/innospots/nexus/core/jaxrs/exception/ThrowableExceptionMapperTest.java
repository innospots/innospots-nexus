package com.innospots.nexus.core.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.jaxrs.support.HttpHeaderNames;

class ThrowableExceptionMapperTest {

    @Test
    void mapsNestedNexusExceptionWithoutSystemErrorWrapper() {
        JaxRsExceptionSupport support = new JaxRsExceptionSupport((httpStatus, body, requestId) -> {
            assertThat(httpStatus).isEqualTo(NexusStatusCode.INVALID_PARAMETER.httpStatusCode());
            return stubResponse(httpStatus, requestId, body);
        });
        ThrowableExceptionMapper mapper = new ThrowableExceptionMapper(support);

        NexusException business = NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        RuntimeException wrapper = new RuntimeException("proxy", business);

        Response response = mapper.toResponse(wrapper);

        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.httpStatusCode());
        assertThat(response.getEntity()).isInstanceOfSatisfying(R.class, body -> {
            assertThat(body.success()).isFalse();
            assertThat(body.code()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
        });
    }

    @Test
    void mapsProgrammingErrorToSystemError() {
        JaxRsExceptionSupport support = new JaxRsExceptionSupport((httpStatus, body, requestId) -> {
            assertThat(httpStatus).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
            return stubResponse(httpStatus, requestId, body);
        });
        ThrowableExceptionMapper mapper = new ThrowableExceptionMapper(support);

        Response response = mapper.toResponse(new NullPointerException("securityRealm"));

        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
        assertThat(response.getEntity()).isInstanceOfSatisfying(R.class, body -> assertThat(body.code())
                .isEqualTo(NexusStatusCode.SYSTEM_ERROR.fullCode()));
    }

    private static Response stubResponse(int httpStatus, String requestId, R<Void> body) {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(httpStatus);
        when(response.getHeaderString(HttpHeaderNames.REQUEST_ID)).thenReturn(requestId);
        when(response.getEntity()).thenReturn(body);
        return response;
    }
}
