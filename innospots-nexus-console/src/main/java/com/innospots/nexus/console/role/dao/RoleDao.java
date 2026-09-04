package com.innospots.nexus.console.role.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.role.domain.entity.RoleEntity;

/**
 * MyBatis-Plus mapper for role records.
 */
@Mapper
public interface RoleDao extends BaseMapper<RoleEntity> {
}
