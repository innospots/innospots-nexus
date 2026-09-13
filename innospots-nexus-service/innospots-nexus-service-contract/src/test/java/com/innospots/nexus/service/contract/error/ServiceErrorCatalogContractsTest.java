package com.innospots.nexus.service.contract.error;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceErrorCatalogContractsTest {

    @Test
    void resolvesRegisteredServiceAndPlatformCodes() {
        ServiceErrorCatalog catalog = ServiceErrorCatalog.standard();

        ServiceError contextMissing = catalog.resolve(ServiceStatusCode.CONTEXT_UNAVAILABLE.fullCode());
        assertThat(contextMissing.code()).isEqualTo("SRV080001");
        assertThat(contextMissing.httpStatus()).isEqualTo(500);
        assertThat(contextMissing.retryable()).isFalse();
        assertThat(contextMissing.message()).contains("Service context unavailable");

        ServiceError limit = catalog.resolve(NexusStatusCode.LIMIT_EXCEEDED.fullCode());
        assertThat(limit.code()).isEqualTo("NEX100012");
        assertThat(limit.httpStatus()).isEqualTo(429);
        assertThat(limit.retryable()).isTrue();
    }

    @Test
    void mapsUnknownCodesToSystemErrorWithoutGuessingHttpFromPrefix() {
        ServiceErrorCatalog catalog = ServiceErrorCatalog.standard();

        ServiceError unknown = catalog.resolve("ZZZ019999");
        assertThat(unknown.code()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.fullCode());
        assertThat(unknown.httpStatus()).isEqualTo(500);
        assertThat(unknown.retryable()).isFalse();
    }

    @Test
    void rejectsDuplicateFullCodesAtConstruction() {
        assertThatThrownBy(() -> ServiceErrorCatalog.of(
                        NexusStatusCode.SYSTEM_ERROR, NexusStatusCode.SYSTEM_ERROR))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.CONFIG_ERROR.fullCode());
    }
}
