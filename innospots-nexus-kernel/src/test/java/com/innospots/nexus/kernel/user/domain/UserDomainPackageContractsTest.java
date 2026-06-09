package com.innospots.nexus.kernel.user.domain;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.request.BasePageRequest;
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
    void userPageRequestExtendsBasePageRequestAndProvidesUserFilters() {
        UserPageRequest request = new UserPageRequest();

        request.setInput("common");
        request.setUserName("alice");
        request.setRealName("Alice Lee");
        request.setEmail("alice@example.com");
        request.setMobile("13800000000");

        assertThat(request).isInstanceOf(BasePageRequest.class);
        assertThat(request.getInput()).isEqualTo("common");
        assertThat(request.getUserName()).isEqualTo("alice");
        assertThat(request.getRealName()).isEqualTo("Alice Lee");
        assertThat(request.getEmail()).isEqualTo("alice@example.com");
        assertThat(request.getMobile()).isEqualTo("13800000000");
    }

    private static void assertPackageExists(String packageName) throws ClassNotFoundException {
        assertThat(Class.forName(packageName + ".package-info").getPackage())
                .as(packageName)
                .isNotNull();
    }
}
