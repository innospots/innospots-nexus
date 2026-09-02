package com.innospots.nexus.core.plugin.installation.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.innospots.nexus.core.plugin.installation.domain.entity.PluginInstallationEntity;
import org.apache.ibatis.annotations.Mapper;

/** 只访问 nx_plugin_installation 单表的 DAO。 */
@Mapper
public interface PluginInstallationDao extends BaseMapper<PluginInstallationEntity> {

    /**
     * 查询全部安装事实。
     *
     * @return 全部安装记录
     */
    default List<PluginInstallationEntity> selectAll() {
        return selectList(null);
    }

    /**
     * 按稳定 pluginId 查询安装事实。
     *
     * @param pluginId 稳定的插件标识
     * @return 匹配的安装记录；标识为空或未找到时返回 null
     */
    default PluginInstallationEntity selectByPluginId(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<PluginInstallationEntity>()
                .eq(PluginInstallationEntity::getPluginId, pluginId));
    }
}
