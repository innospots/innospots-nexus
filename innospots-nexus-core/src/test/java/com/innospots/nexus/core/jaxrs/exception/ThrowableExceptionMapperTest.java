package com.innospots.nexus.core.jaxrs.exception;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import static org.assertj.core.api.Assertions.assertThat;

class ThrowableExceptionMapperTest {

    private final ThrowableExceptionMapper mapper = new ThrowableExceptionMapper(new JaxRsExceptionSupport());

    @Test
    void mapsNestedNexusExceptionWithoutSystemErrorWrapper() {
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
        Response response = mapper.toResponse(new NullPointerException("securityRealm"));

        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
        assertThat(response.getEntity()).isInstanceOfSatisfying(R.class, body -> assertThat(body.code())
                .isEqualTo(NexusStatusCode.SYSTEM_ERROR.fullCode()));
    }
}
