package com.innospots.nexus.kernel.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPackageContractsTest {

    @Test
    void userModuleDefinesApplicationAndDataOperationPackages() throws ClassNotFoundException {
        assertPackageExists("com.innospots.nexus.kernel.user.tools");
        assertPackageExists("com.innospots.nexus.kernel.user.dao");
        assertPackageExists("com.innospots.nexus.kernel.user.operator");
        assertPackageExists("com.innospots.nexus.kernel.user.domain");
        assertPackageExists("com.innospots.nexus.kernel.user.domain.enums");
    }

    @Test
    void userOperatorLivesInOperatorPackage() throws ClassNotFoundException {
        assertThat(Class.forName("com.innospots.nexus.kernel.user.operator.UserOperator"))
                .isNotInterface();
        assertThat(Class.forName("com.innospots.nexus.kernel.user.operator.UserOauthOperator"))
                .isNotInterface();
    }

    @Test
    void userToolsProvideFrontendPasswordDecryptionContract() throws ClassNotFoundException {
        assertThat(Class.forName("com.innospots.nexus.kernel.user.tools.UserPasswordDecryptor"))
                .isInterface();
        assertThat(Class.forName("com.innospots.nexus.kernel.user.tools.RsaUserPasswordDecryptor"))
                .isNotInterface();
    }

    @Test
    void userDaoLivesInDaoPackage() throws ClassNotFoundException {
        assertThat(Class.forName("com.innospots.nexus.kernel.user.dao.UserDao"))
                .isInterface();
        assertThat(Class.forName("com.innospots.nexus.kernel.user.dao.UserPasswordCredentialDao"))
                .isInterface();
        assertThat(Class.forName("com.innospots.nexus.kernel.user.dao.UserOauthIdentityDao"))
                .isInterface();
    }

    private static void assertPackageExists(String packageName) throws ClassNotFoundException {
        assertThat(Class.forName(packageName + ".package-info").getPackage())
                .as(packageName)
                .isNotNull();
    }
}
