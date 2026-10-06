package com.innospots.nexus.platform.organization.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.platform.organization.domain.entity.TenantEntity;

/**
 * {@link com.innospots.nexus.platform.organization.domain.entity.TenantEntity} 的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.organization.operator.TenantOperator
 */
@Mapper
public interface TenantDao extends BaseMapper<TenantEntity> {
}
