package com.innospots.nexus.console.user.admin.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;

/**
 * 平台用户记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface AdminUserDao extends BaseMapper<AdminUserEntity> {
}
