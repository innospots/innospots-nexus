package com.innospots.nexus.kernel.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.kernel.user.domain.entity.UserEntity;
import com.innospots.nexus.kernel.user.domain.entity.UserOauthIdentityEntity;
import com.innospots.nexus.kernel.user.domain.entity.UserPasswordCredentialEntity;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoContractsTest {

    @Test
    void userDaosExposeMybatisPlusBaseMapperContracts() {
        assertThat(BaseMapper.class).isAssignableFrom(UserDao.class);
        assertThat(BaseMapper.class).isAssignableFrom(UserPasswordCredentialDao.class);
        assertThat(BaseMapper.class).isAssignableFrom(UserOauthIdentityDao.class);
    }

    @Test
    void userDaosBindToExpectedEntities() {
        assertMapperEntity(UserDao.class, UserEntity.class);
        assertMapperEntity(UserPasswordCredentialDao.class, UserPasswordCredentialEntity.class);
        assertMapperEntity(UserOauthIdentityDao.class, UserOauthIdentityEntity.class);
    }

    private static void assertMapperEntity(Class<?> mapperType, Class<?> entityType) {
        assertThat(mapperType.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<" + entityType.getName() + ">"));
    }
}
