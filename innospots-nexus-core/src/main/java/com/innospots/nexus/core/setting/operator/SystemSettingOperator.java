package com.innospots.nexus.core.setting.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.core.setting.dao.SystemSettingDao;
import com.innospots.nexus.core.setting.domain.entity.SystemSettingEntity;
import com.innospots.nexus.core.setting.domain.enums.SettingScope;
import com.innospots.nexus.core.setting.domain.enums.SettingValueType;

/**
 * {@code nx_system_setting} 表读写；不含格式校验与 {@link SettingValueType} 门禁（见
 * {@link com.innospots.nexus.core.setting.service.SystemSettingService}）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.dao.SystemSettingDao
 */
@RequiredArgsConstructor
public class SystemSettingOperator {

    private final SystemSettingDao settingDao;

    /**
     * 按域、范围与键查询唯一设置行。
     *
     * @param settingDomain 设置域
     * @param scope         作用范围
     * @param scopeId       范围 ID；GLOBAL 时可 null
     * @param settingKey    设置键
     * @return 存在时的实体，否则 {@code null}
     */
    public SystemSettingEntity find(String settingDomain, SettingScope scope, String scopeId, String settingKey) {
        return settingDao.selectOne(new LambdaQueryWrapper<SystemSettingEntity>()
                .eq(SystemSettingEntity::getSettingDomain, settingDomain)
                .eq(SystemSettingEntity::getSettingScope, scope.name())
                .eq(SystemSettingEntity::getScopeId, normalizeScopeId(scopeId))
                .eq(SystemSettingEntity::getSettingKey, settingKey));
    }

    /**
     * 插入新设置项（含值类型，后续更新不改类型）。
     *
     * @param settingDomain 设置域
     * @param scope         作用范围
     * @param scopeId       范围 ID
     * @param settingKey    设置键
     * @param valueType     值类型
     * @param settingValue  已 trim 的值
     * @return 插入后的实体（含生成主键与审计字段）
     */
    public SystemSettingEntity insert(
            String settingDomain,
            SettingScope scope,
            String scopeId,
            String settingKey,
            SettingValueType valueType,
            String settingValue) {
        SystemSettingEntity entity = new SystemSettingEntity();
        entity.setSettingDomain(settingDomain);
        entity.setSettingScope(scope.name());
        entity.setScopeId(normalizeScopeId(scopeId));
        entity.setSettingKey(settingKey);
        entity.setValueType(valueType.name());
        entity.setSettingValue(settingValue);
        settingDao.insert(entity);
        return entity;
    }

    /**
     * 更新已有行的 {@code setting_value}。
     *
     * @param entity       已加载实体
     * @param settingValue 新值
     * @return 同一实体引用
     */
    public SystemSettingEntity updateValue(SystemSettingEntity entity, String settingValue) {
        entity.setSettingValue(settingValue);
        settingDao.updateById(entity);
        return entity;
    }

    /**
     * 将 null/空白 scopeId 规范为 {@link SystemSettingEntity#GLOBAL_SCOPE_ID}。
     */
    static String normalizeScopeId(String scopeId) {
        if (scopeId == null || scopeId.isBlank()) {
            return SystemSettingEntity.GLOBAL_SCOPE_ID;
        }
        return scopeId.trim();
    }
}
