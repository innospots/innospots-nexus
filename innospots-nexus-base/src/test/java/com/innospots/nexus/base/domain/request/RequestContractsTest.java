package com.innospots.nexus.base.domain.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestContractsTest {

    @Test
    void basePageRequestProvidesInputAndPaginationDefaults() {
        BasePageRequest request = new BasePageRequest();

        assertThat(request.getInput()).isNull();
        assertThat(request.getPageNo()).isEqualTo(1L);
        assertThat(request.getPageSize()).isEqualTo(20L);
    }

    @Test
    void basePageRequestAllowsLombokGeneratedAccessorsToAssignPagination() {
        BasePageRequest request = new BasePageRequest();

        request.setInput("alice");
        request.setPageNo(2L);
        request.setPageSize(50L);

        assertThat(request.getInput()).isEqualTo("alice");
        assertThat(request.getPageNo()).isEqualTo(2L);
        assertThat(request.getPageSize()).isEqualTo(50L);
    }
}
