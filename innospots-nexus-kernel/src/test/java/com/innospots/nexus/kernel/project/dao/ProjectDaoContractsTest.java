package com.innospots.nexus.kernel.project.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.kernel.project.domain.entity.ProjectEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectDaoContractsTest {

    @Test
    void projectDaoBindsProjectEntity() {
        assertThat(BaseMapper.class).isAssignableFrom(ProjectDao.class);
        assertThat(ProjectDao.class.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + ProjectEntity.class.getName() + ">"));
    }
}
