package com.innospots.nexus.console.user.portal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPackageContractsTest {

    @Test
    void userModuleDefinesExpectedSubPackages() throws ClassNotFoundException {
        // 以各类所在包验证包结构，避免依赖 package-info 占位文件。
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.dao.PortalUserDao").getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.dao");
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.operator.PortalUserOauthOperator")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.operator");
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.service.PortalUserService")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.service");
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.service.PortalPasswordService")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.service");
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.domain.entity.PortalUserEntity")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.domain.entity");
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.domain.enums.PortalUserStatus")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.domain.enums");
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.domain.vo.PortalUserProfileVo")
                .getPackageName())
                .isEqualTo("com.innospots.nexus.console.user.portal.domain.vo");
    }

    @Test
    void crossDomainOrchestrationLivesInServicePackage() throws ClassNotFoundException {
        // 依赖凭据域的类必须归 service，operator 只做单表持久化。
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.service.PortalUserService"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.service.PortalPasswordService"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.operator.PortalUserOauthOperator"))
                .isNotInterface();
    }

    @Test
    void userDaoLivesInDaoPackage() throws ClassNotFoundException {
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.dao.PortalUserDao"))
                .isInterface();
        assertThat(Class.forName("com.innospots.nexus.console.user.portal.dao.PortalUserOauthIdentityDao"))
                .isInterface();
    }
}
