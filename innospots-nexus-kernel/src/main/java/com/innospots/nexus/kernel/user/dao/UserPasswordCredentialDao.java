package com.innospots.nexus.kernel.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.kernel.user.domain.entity.UserPasswordCredentialEntity;

/**
 * MyBatis-Plus mapper for local password credential records.
 */
public interface UserPasswordCredentialDao extends BaseMapper<UserPasswordCredentialEntity> {

    default UserPasswordCredentialEntity getByUserId(String userId) {
        if (userId == null) {
            return null;
        }
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserPasswordCredentialEntity>()
                .eq(UserPasswordCredentialEntity::getUserId, userId)
        );
    }
}
