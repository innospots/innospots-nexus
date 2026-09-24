package com.innospots.nexus.portal.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPackageContractsTest {

    @Test
    void userModuleDefinesApplicationAndDataOperationPackages() throws ClassNotFoundException {
        assertPackageExists("com.innospots.nexus.portal.user.tools");
        assertPackageExists("com.innospots.nexus.portal.user.dao");
        assertPackageExists("com.innospots.nexus.portal.user.operator");
        assertPackageExists("com.innospots.nexus.portal.user.domain");
        assertPackageExists("com.innospots.nexus.portal.user.domain.enums");
    }

    @Test
    void userOperatorLivesInOperatorPackage() throws ClassNotFoundException {
        assertThat(Class.forName("com.innospots.nexus.portal.user.operator.UserOperator"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.portal.user.operator.UserOauthOperator"))
                .isNotInterface();
    }

    @Test
    void userDaoLivesInDaoPackage() throws ClassNotFoundException {
        assertThat(Class.forName("com.innospots.nexus.portal.user.dao.UserDao"))
                .isInterface();
        assertThat(Class.forName("com.innospots.nexus.portal.user.dao.UserOauthIdentityDao"))
                .isInterface();
    }

    private static void assertPackageExists(String packageName) throws ClassNotFoundException {
        assertThat(Class.forName(packageName + ".package-info").getPackage())
                .as(packageName)
                .isNotNull();
    }
}
