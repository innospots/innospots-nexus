package com.innospots.nexus.core.setting.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.innospots.nexus.core.setting.domain.entity.SystemSettingEntity;

/**
 * {@link SystemSettingEntity} 的 MyBatis-Plus Mapper；无自定义 SQL，依赖唯一键与主键 CRUD。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.operator.SystemSettingOperator
 */
@Mapper
public interface SystemSettingDao extends BaseMapper<SystemSettingEntity> {
}
