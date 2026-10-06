package com.innospots.nexus.core.setting.domain.enums;

/**
 * 系统设置作用范围（持久化为 {@code setting_scope}）。
 *
 * <p>{@link #GLOBAL} 表示全实例单值；后续可扩展租户、工作区等范围并配合非空 {@code scope_id}。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.domain.entity.SystemSettingEntity#scopeId
 */
public enum SettingScope {

    /** 实例级全局设置（{@code scope_id} 为空串）。 */
    GLOBAL
}
