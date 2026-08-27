package com.innospots.nexus.base.domain.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationTest {

    @Test
    void normalizesInvalidPageNumberAndSizeToDefaults() {
        assertThat(Pagination.normalizePageNo(0L)).isEqualTo(Pagination.DEFAULT_PAGE_NO);
        assertThat(Pagination.normalizePageNo(-3L)).isEqualTo(1L);
        assertThat(Pagination.normalizePageSize(0L)).isEqualTo(Pagination.DEFAULT_PAGE_SIZE);
        assertThat(Pagination.normalizePageSize(-1L)).isEqualTo(20L);
    }

    @Test
    void keepsValidPageNumberAndSize() {
        assertThat(Pagination.normalizePageNo(3L)).isEqualTo(3L);
        assertThat(Pagination.normalizePageSize(50L)).isEqualTo(50L);
    }
}
