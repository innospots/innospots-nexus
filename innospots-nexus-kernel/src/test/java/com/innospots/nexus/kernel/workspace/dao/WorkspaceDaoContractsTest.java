package com.innospots.nexus.kernel.workspace.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.kernel.workspace.domain.entity.WorkspaceEntity;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceDaoContractsTest {

    @Test
    void workspaceDaoBindsWorkspaceEntity() {
        assertThat(BaseMapper.class).isAssignableFrom(WorkspaceDao.class);
        assertThat(WorkspaceDao.class.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + WorkspaceEntity.class.getName() + ">"));
    }
}
