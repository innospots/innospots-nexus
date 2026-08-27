package com.innospots.nexus.console.extension.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.console.extension.domain.entity.ExtensionInstallationEntity;

/** Single-table DAO for extension installation and registration records. */
public interface ExtensionInstallationDao extends BaseMapper<ExtensionInstallationEntity> {

    /** Finds all installation records for management views and startup reconciliation. */
    default List<ExtensionInstallationEntity> selectAll() {
        return selectList(null);
    }

    /** Finds one installation record by its stable extension key. */
    default ExtensionInstallationEntity selectByExtensionKey(String extensionKey) {
        if (extensionKey == null || extensionKey.isBlank()) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<ExtensionInstallationEntity>()
                .eq(ExtensionInstallationEntity::getExtensionKey, extensionKey));
    }
}
