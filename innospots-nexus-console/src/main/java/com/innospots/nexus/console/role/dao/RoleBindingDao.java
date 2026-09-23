package com.innospots.nexus.console.role.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;

/**
 * 角色绑定记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface RoleBindingDao extends BaseMapper<RoleBindingEntity> {
}
