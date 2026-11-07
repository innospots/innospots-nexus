package com.innospots.nexus.console.scope.workspace.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceEntity;

/**
 * 工作区记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface WorkspaceDao extends BaseMapper<WorkspaceEntity> {
}
