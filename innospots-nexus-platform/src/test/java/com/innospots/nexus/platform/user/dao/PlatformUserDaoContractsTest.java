package com.innospots.nexus.platform.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserOauthEntity;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserPasswordEntity;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformUserDaoContractsTest {

    @Test
    void platformUserDaosBindExpectedEntities() {
        assertMapperEntity(PlatformUserDao.class, PlatformUserEntity.class);
        assertMapperEntity(PlatformUserPasswordDao.class, PlatformUserPasswordEntity.class);
        assertMapperEntity(PlatformUserOauthDao.class, PlatformUserOauthEntity.class);
    }

    private static void assertMapperEntity(Class<?> mapperType, Class<?> entityType) {
        assertThat(BaseMapper.class).isAssignableFrom(mapperType);
        assertThat(mapperType.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + entityType.getName() + ">"));
    }
}
