package com.innospots.nexus.kernel.user.operator;

import java.time.LocalDateTime;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import com.innospots.nexus.kernel.user.enums.UserStatus;
import com.innospots.nexus.kernel.user.dao.UserDao;
import com.innospots.nexus.kernel.user.dao.UserOauthIdentityDao;
import com.innospots.nexus.kernel.user.domain.entity.UserEntity;
import com.innospots.nexus.kernel.user.domain.entity.UserOauthIdentityEntity;
import com.innospots.nexus.kernel.user.domain.request.UserOauthRegisterRequest;
import com.innospots.nexus.kernel.user.domain.vo.UserProfileVO;
import com.innospots.nexus.kernel.user.enums.UserRegisterSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserOauthOperatorTest {

    @Test
    void userOauthOperatorExposesLombokGeneratedConstructorAndLogger() throws Exception {
        assertThat(UserOauthOperator.class.getDeclaredConstructor(
                UserDao.class,
                UserOauthIdentityDao.class)).isNotNull();
        assertThat(UserOauthOperator.class.getDeclaredField("log").getType()).isEqualTo(Logger.class);
    }

    @Test
    void oauthRegistrationDeclaresTransactionalBoundary() throws Exception {
        assertThat(UserOauthOperator.class
                .getDeclaredMethod("registerWithOauth", UserOauthRegisterRequest.class)
                .getAnnotation(Transactional.class)).isNotNull();
    }

    @Test
    void registersOauthUserWithExternalIdentity() {
        UserDao userDao = mock(UserDao.class);
        UserOauthIdentityDao oauthDao = mock(UserOauthIdentityDao.class);
        UserOauthOperator operator = new UserOauthOperator(userDao, oauthDao);

        UserProfileVO profile = operator.registerWithOauth(new UserOauthRegisterRequest(
                "carol",
                "Carol",
                "Carol Smith",
                "carol@example.com",
                null,
                "github",
                "gh-1003",
                "carol-gh",
                "Carol GH",
                "carol@github.example",
                "https://example.test/avatar.png",
                "access-key",
                "refresh-key",
                LocalDateTime.now().plusDays(1)));

        assertThat(profile.userId()).startsWith("usr");
        assertThat(profile.userId()).hasSize(29);
        assertThat(profile.registerSource()).isEqualTo(UserRegisterSource.OAUTH);
        assertThat(profile.status()).isEqualTo(UserStatus.ACTIVE);
        ArgumentCaptor<UserEntity> userCaptor = forClass(UserEntity.class);
        verify(userDao).insert(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserId()).isEqualTo(profile.userId());
        ArgumentCaptor<UserOauthIdentityEntity> identityCaptor = forClass(UserOauthIdentityEntity.class);
        verify(oauthDao).insert(identityCaptor.capture());
        assertThat(identityCaptor.getValue().getIdentityId()).startsWith("uoi");
        assertThat(identityCaptor.getValue().getIdentityId()).hasSize(29);
        assertThat(identityCaptor.getValue().getUserId()).isEqualTo(profile.userId());
    }
}
