package com.innospots.nexus.console.user.portal.domain;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.request.SimpleQueryRequest;
import com.innospots.nexus.console.user.portal.domain.request.PortalUserPageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class UserDomainPackageContractsTest {

    @Test
    void userDomainDefinesExpectedSubPackages() throws ClassNotFoundException {
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.domain.entity.PortalUserEntity")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.domain.entity");
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.domain.vo.PortalUserProfileVo")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.domain.vo");
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.domain.request.PortalUserPageRequest")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.domain.request");
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.domain.model.PortalUser")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.domain.model");
    }

    @Test
    void userPageRequestProvidesUserFiltersWithPaginationDefaults() {
        PortalUserPageRequest request = new PortalUserPageRequest(
                "common", 1L, 20L, "alice", "Alice Lee",
                "alice@example.com", "13800000000");

        assertThat(request.input()).isEqualTo("common");
        assertThat(request.pageNo()).isEqualTo(SimpleQueryRequest.DEFAULT_PAGE_NO);
        assertThat(request.pageSize()).isEqualTo(SimpleQueryRequest.DEFAULT_PAGE_SIZE);
        assertThat(request.userName()).isEqualTo("alice");
        assertThat(request.displayName()).isEqualTo("Alice Lee");
        assertThat(request.email()).isEqualTo("alice@example.com");
        assertThat(request.mobile()).isEqualTo("13800000000");
    }
}
