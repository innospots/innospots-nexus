package com.innospots.nexus.kernel.user.domain;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.request.SimpleQueryRequest;
import com.innospots.nexus.kernel.user.domain.request.UserPageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class UserDomainPackageContractsTest {

    @Test
    void userDomainDefinesExpectedSubPackages() throws ClassNotFoundException {
        assertPackageExists("com.innospots.nexus.kernel.user.domain.entity");
        assertPackageExists("com.innospots.nexus.kernel.user.domain.vo");
        assertPackageExists("com.innospots.nexus.kernel.user.domain.request");
        assertPackageExists("com.innospots.nexus.kernel.user.domain.model");
    }

    @Test
    void userPageRequestProvidesUserFiltersWithPaginationDefaults() {
        UserPageRequest request = new UserPageRequest(
                "common", 1L, 20L, "alice", "Alice Lee",
                "alice@example.com", "13800000000");

        assertThat(request.input()).isEqualTo("common");
        assertThat(request.pageNo()).isEqualTo(SimpleQueryRequest.DEFAULT_PAGE_NO);
        assertThat(request.pageSize()).isEqualTo(SimpleQueryRequest.DEFAULT_PAGE_SIZE);
        assertThat(request.userName()).isEqualTo("alice");
        assertThat(request.realName()).isEqualTo("Alice Lee");
        assertThat(request.email()).isEqualTo("alice@example.com");
        assertThat(request.mobile()).isEqualTo("13800000000");
    }

    private static void assertPackageExists(String packageName) throws ClassNotFoundException {
        assertThat(Class.forName(packageName + ".package-info").getPackage())
                .as(packageName)
                .isNotNull();
    }
}
