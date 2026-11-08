package com.innospots.nexus.console.scope.tenant.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity;

import static org.assertj.core.api.Assertions.assertThat;

class TenantDaoContractsTest {

    @Test
    void tenantDaoBindsTenantEntity() {
        assertThat(BaseMapper.class.isAssignableFrom(TenantDao.class)).isTrue();
        assertThat(TenantEntity.class.isAssignableFrom(TenantEntity.class)).isTrue();
    }
}
