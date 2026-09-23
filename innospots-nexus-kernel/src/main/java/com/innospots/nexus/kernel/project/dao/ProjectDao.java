package com.innospots.nexus.kernel.project.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.kernel.project.domain.entity.ProjectEntity;

/**
 * 项目记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface ProjectDao extends BaseMapper<ProjectEntity> {
}
