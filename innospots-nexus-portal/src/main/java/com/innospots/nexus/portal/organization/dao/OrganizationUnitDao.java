package com.innospots.nexus.portal.organization.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.portal.organization.domain.entity.OrganizationUnitEntity;

/**
 * 组织单元记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface OrganizationUnitDao extends BaseMapper<OrganizationUnitEntity> {
}
