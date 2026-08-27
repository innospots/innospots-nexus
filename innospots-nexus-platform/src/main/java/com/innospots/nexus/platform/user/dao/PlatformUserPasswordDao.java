package com.innospots.nexus.platform.user.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.platform.user.domain.entity.PlatformUserPasswordEntity;

/**
 * MyBatis-Plus mapper for platform user password credentials.
 */
public interface PlatformUserPasswordDao extends BaseMapper<PlatformUserPasswordEntity> {

    /**
     * Finds the password credential for one platform user.
     *
     * @param platformUserId platform-realm user identifier
     * @return credential row, or null when absent
     */
    default PlatformUserPasswordEntity getByUserId(String platformUserId) {
        if (platformUserId == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<PlatformUserPasswordEntity>()
                .eq(PlatformUserPasswordEntity::getPlatformUserId, platformUserId));
    }
}
