package com.innospots.nexus.kernel.user.operator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import com.innospots.nexus.base.domain.data.DataPage;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.kernel.user.UserStatus;
import com.innospots.nexus.kernel.user.api.UserPasswordDecryptor;
import com.innospots.nexus.kernel.user.dao.UserDao;
import com.innospots.nexus.kernel.user.dao.UserOauthIdentityDao;
import com.innospots.nexus.kernel.user.dao.UserPasswordCredentialDao;
import com.innospots.nexus.kernel.user.domain.entity.UserEntity;
import com.innospots.nexus.kernel.user.domain.entity.UserOauthIdentityEntity;
import com.innospots.nexus.kernel.user.domain.entity.UserPasswordCredentialEntity;
import com.innospots.nexus.kernel.user.domain.request.UserOauthRegisterRequest;
import com.innospots.nexus.kernel.user.domain.request.UserPageRequest;
import com.innospots.nexus.kernel.user.domain.request.UserPasswordRegisterRequest;
import com.innospots.nexus.kernel.user.domain.vo.UserProfileVO;
import com.innospots.nexus.kernel.user.enums.UserRegisterSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserOperatorTest {

    @Test
    void userOperatorExposesLombokGeneratedConstructorAndLogger() throws Exception {
        assertThat(UserOperator.class.getDeclaredConstructor(
                UserDao.class,
                UserPasswordCredentialDao.class,
                UserOauthIdentityDao.class,
                UserPasswordDecryptor.class)).isNotNull();
        assertThat(UserOperator.class.getDeclaredField("log").getType()).isEqualTo(Logger.class);
    }

    @Test
    void writeOperationsDeclareTransactionalBoundary() throws Exception {
        assertThat(UserOperator.class
                .getDeclaredMethod("registerWithPassword", UserPasswordRegisterRequest.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(UserOperator.class
                .getDeclaredMethod("registerWithOauth", UserOauthRegisterRequest.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(UserOperator.class
                .getDeclaredMethod("deleteUser", Long.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(UserOperator.class
                .getDeclaredMethod("freezeUser", Long.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(UserOperator.class
                .getDeclaredMethod("findById", Long.class)
                .getAnnotation(Transactional.class)).isNull();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void pagesUsersWithRequestFilters() {
        UserDao userDao = mock(UserDao.class);
        IPage<UserEntity> selectedPage = mock(IPage.class);
        UserEntity entity = new UserEntity();
        entity.setUserId(1004L);
        entity.setUserName("dave");
        entity.setRealName("Dave Lee");
        entity.setEmail("dave@example.com");
        entity.setMobile("13900000000");
        entity.setRegisterSource(UserRegisterSource.PASSWORD.name());
        entity.setStatus(UserStatus.ACTIVE.name());
        when(selectedPage.getRecords()).thenReturn(List.of(entity));
        when(selectedPage.getTotal()).thenReturn(1L);
        when(userDao.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(selectedPage);
        UserOperator operator = new UserOperator(
                userDao,
                mock(UserPasswordCredentialDao.class),
                mock(UserOauthIdentityDao.class),
                mock(UserPasswordDecryptor.class));
        UserPageRequest request = new UserPageRequest();
        request.setPageNo(2L);
        request.setPageSize(5L);
        request.setUserName("dav");
        request.setRealName("Lee");
        request.setEmail("example.com");
        request.setMobile("139");

        DataPage<UserProfileVO> page = operator.pageUsers(request);

        assertThat(page.pageNo()).isEqualTo(2L);
        assertThat(page.pageSize()).isEqualTo(5L);
        assertThat(page.total()).isEqualTo(1L);
        assertThat(page.records()).hasSize(1);
        ArgumentCaptor<IPage> pageCaptor = forClass(IPage.class);
        ArgumentCaptor<Wrapper> wrapperCaptor = forClass(Wrapper.class);
        verify(userDao).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2L);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(5L);
        assertThat(wrapperCaptor.getValue().getCustomSqlSegment())
                .contains("user_name", "real_name", "email", "mobile");
    }

    @Test
    void deletesUserById() {
        UserDao userDao = mock(UserDao.class);
        when(userDao.deleteById(1005L)).thenReturn(1);
        UserOperator operator = new UserOperator(
                userDao,
                mock(UserPasswordCredentialDao.class),
                mock(UserOauthIdentityDao.class),
                mock(UserPasswordDecryptor.class));

        assertThat(operator.deleteUser(1005L)).isTrue();

        verify(userDao).deleteById(1005L);
    }

    @Test
    void freezesUserByDisablingStatus() {
        UserDao userDao = mock(UserDao.class);
        when(userDao.updateById(any(UserEntity.class))).thenReturn(1);
        UserOperator operator = new UserOperator(
                userDao,
                mock(UserPasswordCredentialDao.class),
                mock(UserOauthIdentityDao.class),
                mock(UserPasswordDecryptor.class));

        assertThat(operator.freezeUser(1006L)).isTrue();

        ArgumentCaptor<UserEntity> userCaptor = forClass(UserEntity.class);
        verify(userDao).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserId()).isEqualTo(1006L);
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.DISABLED.name());
    }

    @Test
    void findsUserProfileById() {
        UserDao userDao = mock(UserDao.class);
        UserEntity entity = new UserEntity();
        entity.setUserId(1001L);
        entity.setUserName("alice");
        entity.setDisplayName("Alice");
        entity.setEmail("alice@example.com");
        entity.setRegisterSource(UserRegisterSource.PASSWORD.name());
        entity.setStatus(UserStatus.ACTIVE.name());
        when(userDao.selectById(1001L)).thenReturn(entity);

        UserOperator operator = new UserOperator(
                userDao,
                mock(UserPasswordCredentialDao.class),
                mock(UserOauthIdentityDao.class),
                mock(UserPasswordDecryptor.class));

        Optional<UserProfileVO> profile = operator.findById(1001L);

        assertThat(profile).hasValueSatisfying(user -> {
            assertThat(user.userId()).isEqualTo(1001L);
            assertThat(user.userName()).isEqualTo("alice");
            assertThat(user.registerSource()).isEqualTo(UserRegisterSource.PASSWORD);
            assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
        });
    }

    @Test
    void registersPasswordUserWithSeparatePasswordCredential() {
        UserDao userDao = mock(UserDao.class);
        UserPasswordCredentialDao credentialDao = mock(UserPasswordCredentialDao.class);
        UserPasswordDecryptor passwordDecryptor = mock(UserPasswordDecryptor.class);
        UserOperator operator = new UserOperator(userDao, credentialDao, mock(UserOauthIdentityDao.class),
                passwordDecryptor);
        when(userDao.insert(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setUserId(1002L);
            return 1;
        });
        when(passwordDecryptor.decrypt(eq("front-encrypted-password"))).thenReturn("raw-secret");

        UserProfileVO profile = operator.registerWithPassword(new UserPasswordRegisterRequest(
                "bob",
                "Bob",
                "Bob Lee",
                "bob@example.com",
                "13800000000",
                "front-encrypted-password"));

        assertThat(profile.userId()).isEqualTo(1002L);
        assertThat(profile.registerSource()).isEqualTo(UserRegisterSource.PASSWORD);
        verify(userDao).insert(any(UserEntity.class));
        verify(passwordDecryptor).decrypt("front-encrypted-password");

        ArgumentCaptor<UserPasswordCredentialEntity> credentialCaptor = forClass(UserPasswordCredentialEntity.class);
        verify(credentialDao).insert(credentialCaptor.capture());
        UserPasswordCredentialEntity credential = credentialCaptor.getValue();
        assertThat(credential.getPasswordHash()).isNotEqualTo("raw-secret");
        assertThat(CryptoUtils.matchesPassword("raw-secret", credential.getPasswordHash())).isTrue();
        assertThat(credential.getPasswordSalt()).isNotBlank();
        assertThat(credential.getPasswordHash())
                .isEqualTo(CryptoUtils.encryptPassword("raw-secret", credential.getPasswordSalt()));
        assertThat(credential.getPasswordAlgorithm()).isEqualTo("BCRYPT");
        assertThat(credential.getPasswordVersion()).isEqualTo(1);
    }

    @Test
    void registersOauthUserWithExternalIdentity() {
        UserDao userDao = mock(UserDao.class);
        UserOauthIdentityDao oauthDao = mock(UserOauthIdentityDao.class);
        UserOperator operator = new UserOperator(userDao, mock(UserPasswordCredentialDao.class), oauthDao,
                mock(UserPasswordDecryptor.class));
        when(userDao.insert(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setUserId(1003L);
            return 1;
        });

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

        assertThat(profile.userId()).isEqualTo(1003L);
        assertThat(profile.registerSource()).isEqualTo(UserRegisterSource.OAUTH);
        verify(userDao).insert(any(UserEntity.class));
        verify(oauthDao).insert(any(UserOauthIdentityEntity.class));
    }
}
