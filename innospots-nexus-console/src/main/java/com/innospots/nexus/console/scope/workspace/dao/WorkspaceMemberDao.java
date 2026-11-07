package com.innospots.nexus.console.scope.workspace.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceMemberEntity;

/**
 * 工作区成员关系记录的 MyBatis-Plus Mapper（{@code nx_workspace_member}）。
 *
 * @author Smars
 * @date 2026/11/07
 */
@Mapper
public interface WorkspaceMemberDao extends BaseMapper<WorkspaceMemberEntity> {
}
