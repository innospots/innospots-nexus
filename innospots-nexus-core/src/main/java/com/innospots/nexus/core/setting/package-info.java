/**
 * 实例级系统设置：按设置域（{@code settingDomain}）、
 * {@link com.innospots.nexus.core.setting.domain.enums.SettingScope} 与
 * {@link com.innospots.nexus.core.setting.domain.enums.SettingValueType} 持久化键值配置。
 *
 * <p>表 {@code nx_system_setting}；管理端 REST 由各上层模块（platform、console 等）暴露，
 * core 仅提供 {@link com.innospots.nexus.core.setting.service.SystemSettingService} 与持久化原语。
 * 业务模块通过 {@link com.innospots.nexus.core.setting.support.SettingKey} 注册域、键与解析器。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.domain.entity.SystemSettingEntity
 * @see com.innospots.nexus.core.setting.service.SystemSettingService
 */
package com.innospots.nexus.core.setting;
