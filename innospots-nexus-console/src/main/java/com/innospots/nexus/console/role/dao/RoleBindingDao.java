package com.innospots.nexus.console.role.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;

/**
 * MyBatis-Plus mapper for role-binding records.
 */
@Mapper
public interface RoleBindingDao extends BaseMapper<RoleBindingEntity> {
}
