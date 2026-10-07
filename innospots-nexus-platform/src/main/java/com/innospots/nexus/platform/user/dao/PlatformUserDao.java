package com.innospots.nexus.platform.user.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;

/**
 * 平台用户记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface PlatformUserDao extends BaseMapper<PlatformUserEntity> {
}
