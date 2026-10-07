package com.innospots.nexus.platform.organization.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.platform.organization.domain.entity.EnterpriseEntity;

/**
 * {@link com.innospots.nexus.platform.organization.domain.entity.EnterpriseEntity} 的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.organization.operator.EnterpriseOperator
 */
@Mapper
public interface EnterpriseDao extends BaseMapper<EnterpriseEntity> {
}
