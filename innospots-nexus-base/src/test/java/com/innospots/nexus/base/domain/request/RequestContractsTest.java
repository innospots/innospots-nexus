package com.innospots.nexus.base.domain.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestContractsTest {

    @Test
    void simplePageRequestProvidesInputAndPaginationDefaults() {
        SimpleQueryRequest request = new SimpleQueryRequest();

        assertThat(request.input()).isNull();
        assertThat(request.pageNo()).isEqualTo(1L);
        assertThat(request.pageSize()).isEqualTo(20L);
    }

    @Test
    void simplePageRequestCanBeConstructedWithExplicitValues() {
        SimpleQueryRequest request = new SimpleQueryRequest("alice", 2L, 50L);

        assertThat(request.input()).isEqualTo("alice");
        assertThat(request.pageNo()).isEqualTo(2L);
        assertThat(request.pageSize()).isEqualTo(50L);
    }

    @Test
    void simplePageRequestNormalizesInvalidPaginationValues() {
        SimpleQueryRequest request = new SimpleQueryRequest(null, 0L, -1L);

        assertThat(request.pageNo()).isEqualTo(1L);
        assertThat(request.pageSize()).isEqualTo(20L);
    }
}
