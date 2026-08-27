package com.innospots.nexus.platform.tenant.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.platform.enterprise.dao.EnterpriseDao;
import com.innospots.nexus.platform.enterprise.domain.entity.EnterpriseEntity;
import com.innospots.nexus.platform.tenant.domain.entity.TenantEntity;

import static org.assertj.core.api.Assertions.assertThat;

class TenantDaoContractsTest {

    @Test
    void tenantAndEnterpriseDaosBindExpectedEntities() {
        assertMapperEntity(TenantDao.class, TenantEntity.class);
        assertMapperEntity(EnterpriseDao.class, EnterpriseEntity.class);
    }

    private static void assertMapperEntity(Class<?> mapperType, Class<?> entityType) {
        assertThat(BaseMapper.class).isAssignableFrom(mapperType);
        assertThat(mapperType.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + entityType.getName() + ">"));
    }
}
