package com.innospots.nexus.console.user.portal.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.console.user.portal.domain.entity.PortalUserEntity;

/**
 * 用户档案记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface PortalUserDao extends BaseMapper<PortalUserEntity> {
}
