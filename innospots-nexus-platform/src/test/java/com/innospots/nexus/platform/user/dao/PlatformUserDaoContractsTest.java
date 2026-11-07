package com.innospots.nexus.console.user.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserOauthEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUserDaoContractsTest {

    @Test
    void platformUserDaosBindExpectedEntities() {
        assertMapperEntity(AdminUserDao.class, AdminUserEntity.class);
        assertMapperEntity(AdminUserOauthDao.class, AdminUserOauthEntity.class);
    }

    private static void assertMapperEntity(Class<?> mapperType, Class<?> entityType) {
        assertThat(BaseMapper.class).isAssignableFrom(mapperType);
        assertThat(mapperType.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + entityType.getName() + ">"));
    }
}
