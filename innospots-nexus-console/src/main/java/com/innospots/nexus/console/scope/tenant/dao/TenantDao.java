package com.innospots.nexus.console.scope.tenant.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity;

/**
 * {@link com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity} 的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.scope.tenant.operator.TenantOperator
 */
@Mapper
public interface TenantDao extends BaseMapper<TenantEntity> {
}
