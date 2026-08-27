package com.innospots.nexus.kernel.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.kernel.member.domain.entity.TenantMemberEntity;

import static org.assertj.core.api.Assertions.assertThat;

class TenantMemberDaoContractsTest {

    @Test
    void tenantMemberDaoBindsTenantMemberEntity() {
        assertThat(BaseMapper.class).isAssignableFrom(TenantMemberDao.class);
        assertThat(TenantMemberDao.class.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + TenantMemberEntity.class.getName() + ">"));
    }
}
