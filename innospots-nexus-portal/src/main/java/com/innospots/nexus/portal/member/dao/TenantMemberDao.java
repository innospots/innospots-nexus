package com.innospots.nexus.portal.member.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.portal.member.domain.entity.TenantMemberEntity;

/**
 * 租户成员关系记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface TenantMemberDao extends BaseMapper<TenantMemberEntity> {
}
