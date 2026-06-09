package com.innospots.nexus.kernel.user.domain.entity;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.entity.BaseEntity;
import com.innospots.nexus.kernel.user.UserStatus;
import com.innospots.nexus.kernel.user.enums.UserRegisterSource;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityContractsTest {

    @Test
    void userEntitiesExposePersistenceTables() {
        assertPersistenceTable(UserEntity.class, "nx_user");
        assertPersistenceTable(UserPasswordCredentialEntity.class, "nx_user_password");
        assertPersistenceTable(UserOauthIdentityEntity.class, "nx_user_oauth");
    }

    @Test
    void userEntitiesInheritCoreBaseEntityForAuditFields() {
        assertThat(UserEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(UserPasswordCredentialEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(UserOauthIdentityEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
    }

    @Test
    void userEntityStoresRegistrationProfileWithoutPasswordSecret() throws NoSuchFieldException {
        assertPersistenceId(UserEntity.class.getDeclaredField("userId"), IdType.ASSIGN_ID);

        assertField(UserEntity.class, "userName", String.class, 64, false);
        assertField(UserEntity.class, "displayName", String.class, 128, true);
        assertField(UserEntity.class, "email", String.class, 128, true);
        assertField(UserEntity.class, "mobile", String.class, 32, true);
        assertField(UserEntity.class, "avatarKey", String.class, 256, true);
        assertField(UserEntity.class, "registerSource", String.class, 32, false);
        assertField(UserEntity.class, "status", String.class, 32, false);
        assertField(UserEntity.class, "lastLoginTime", LocalDateTime.class, 255, true);

        assertThat(List.of(UserEntity.class.getDeclaredFields()))
                .extracting(Field::getName)
                .doesNotContain("password", "passwordHash", "salt");
    }

    @Test
    void passwordCredentialEntityStoresLocalPasswordMaterialSeparately() throws NoSuchFieldException {
        assertPersistenceId(UserPasswordCredentialEntity.class.getDeclaredField("credentialId"), IdType.ASSIGN_ID);

        assertField(UserPasswordCredentialEntity.class, "userId", Long.class, 255, false);
        assertField(UserPasswordCredentialEntity.class, "passwordHash", String.class, 256, false);
        assertField(UserPasswordCredentialEntity.class, "passwordSalt", String.class, 128, false);
        assertField(UserPasswordCredentialEntity.class, "passwordAlgorithm", String.class, 64, false);
        assertField(UserPasswordCredentialEntity.class, "passwordVersion", Integer.class, 255, false);
        assertField(UserPasswordCredentialEntity.class, "expiredAt", LocalDateTime.class, 255, true);
    }

    @Test
    void oauthIdentityEntityAllowsExternalRegistrationWithoutLocalPassword() throws NoSuchFieldException {
        assertPersistenceId(UserOauthIdentityEntity.class.getDeclaredField("identityId"), IdType.ASSIGN_ID);

        assertField(UserOauthIdentityEntity.class, "userId", Long.class, 255, false);
        assertField(UserOauthIdentityEntity.class, "provider", String.class, 64, false);
        assertField(UserOauthIdentityEntity.class, "providerSubject", String.class, 256, false);
        assertField(UserOauthIdentityEntity.class, "providerAccount", String.class, 128, true);
        assertField(UserOauthIdentityEntity.class, "accessTokenKey", String.class, 256, true);
        assertField(UserOauthIdentityEntity.class, "refreshTokenKey", String.class, 256, true);
        assertField(UserOauthIdentityEntity.class, "tokenExpiresAt", LocalDateTime.class, 255, true);
    }

    @Test
    void entitiesCanBeCreatedByMybatisPlusAndJpaReflection() {
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setUserName("alice");
        user.setRegisterSource(UserRegisterSource.PASSWORD.name());
        user.setStatus(UserStatus.ACTIVE.name());

        UserPasswordCredentialEntity password = new UserPasswordCredentialEntity();
        password.setUserId(user.getUserId());
        password.setPasswordHash("hash");
        password.setPasswordSalt("salt");
        password.setPasswordAlgorithm("argon2id");
        password.setPasswordVersion(1);

        UserOauthIdentityEntity oauth = new UserOauthIdentityEntity();
        oauth.setUserId(user.getUserId());
        oauth.setProvider("github");
        oauth.setProviderSubject("gh-1001");

        assertThat(user.getUserName()).isEqualTo("alice");
        assertThat(password.getPasswordAlgorithm()).isEqualTo("argon2id");
        assertThat(oauth.getProviderSubject()).isEqualTo("gh-1001");
    }

    private static void assertPersistenceTable(Class<?> entityType, String tableName) {
        assertThat(entityType.getAnnotation(Entity.class)).isNotNull();
        assertThat(entityType.getAnnotation(Table.class).name()).isEqualTo(tableName);
        assertThat(entityType.getAnnotation(TableName.class).value()).isEqualTo(tableName);
    }

    private static void assertPersistenceId(Field field, IdType idType) {
        assertThat(field.getAnnotation(Id.class)).isNotNull();
        assertThat(field.getAnnotation(TableId.class).type()).isEqualTo(idType);
    }

    private static void assertField(Class<?> entityType, String fieldName, Class<?> fieldType, int length, boolean nullable)
            throws NoSuchFieldException {
        Field field = entityType.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);

        assertThat(field.getType()).isEqualTo(fieldType);
        assertThat(column).isNotNull();
        assertThat(column.name()).isEmpty();
        assertThat(column.length()).isEqualTo(length);
        assertThat(column.nullable()).isEqualTo(nullable);
    }
}
