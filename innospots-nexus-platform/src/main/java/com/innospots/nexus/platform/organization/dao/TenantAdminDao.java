package com.innospots.nexus.platform.organization.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.platform.organization.domain.entity.TenantAdminEntity;

/**
 * {@link TenantAdminEntity} 的 MyBatis-Plus Mapper。
 */
@Mapper
public interface TenantAdminDao extends BaseMapper<TenantAdminEntity> {
}
