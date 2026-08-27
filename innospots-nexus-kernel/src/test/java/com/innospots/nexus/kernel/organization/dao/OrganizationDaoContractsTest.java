package com.innospots.nexus.kernel.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.kernel.organization.domain.entity.OrganizationMemberEntity;
import com.innospots.nexus.kernel.organization.domain.entity.OrganizationUnitEntity;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationDaoContractsTest {

    @Test
    void organizationDaosBindExpectedEntities() {
        assertMapperEntity(OrganizationUnitDao.class, OrganizationUnitEntity.class);
        assertMapperEntity(OrganizationMemberDao.class, OrganizationMemberEntity.class);
    }

    private static void assertMapperEntity(Class<?> mapperType, Class<?> entityType) {
        assertThat(BaseMapper.class).isAssignableFrom(mapperType);
        assertThat(mapperType.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + entityType.getName() + ">"));
    }
}
