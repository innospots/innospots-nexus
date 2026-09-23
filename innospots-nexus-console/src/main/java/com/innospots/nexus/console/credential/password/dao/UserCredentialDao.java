package com.innospots.nexus.console.credential.password.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.innospots.nexus.console.credential.password.domain.entity.UserCredentialEntity;

/**
 * {@link UserCredentialEntity} MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface UserCredentialDao extends BaseMapper<UserCredentialEntity> {
}
