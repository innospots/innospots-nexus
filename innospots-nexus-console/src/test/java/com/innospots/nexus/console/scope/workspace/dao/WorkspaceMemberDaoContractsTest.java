package com.innospots.nexus.console.scope.workspace.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceMemberEntity;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceMemberDaoContractsTest {

    @Test
    void workspaceMemberDaoBindsWorkspaceMemberEntity() {
        assertThat(BaseMapper.class).isAssignableFrom(WorkspaceMemberDao.class);
        assertThat(WorkspaceMemberDao.class.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + WorkspaceMemberEntity.class.getName() + ">"));
    }
}
