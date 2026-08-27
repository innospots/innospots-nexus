package com.innospots.nexus.kernel.user.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.kernel.user.domain.entity.UserPasswordCredentialEntity;

/**
 * MyBatis-Plus mapper for local password credential records.
 */
public interface UserPasswordCredentialDao extends BaseMapper<UserPasswordCredentialEntity> {

    /**
     * Finds the password credential for one tenant-realm user.
     *
     * @param userId tenant-realm user identifier
     * @return credential row, or null when absent
     */
    default UserPasswordCredentialEntity getByUserId(String userId) {
        if (userId == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<UserPasswordCredentialEntity>()
                .eq(UserPasswordCredentialEntity::getTenantUserId, userId));
    }
}
