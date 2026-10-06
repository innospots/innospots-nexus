/**
 * 运营管理平台设置：持久化于 {@link com.innospots.nexus.core.setting.service.SystemSettingService}，
 * 按域 {@code platform} 与 {@link com.innospots.nexus.core.setting.support.SettingKey} 读写。
 *
 * <p>按 {@code domain}、{@code service}、{@code endpoint} 组织；管理端 REST 暴露于
 * {@link com.innospots.nexus.platform.config.PlatformConstant#SETTINGS_PATH} 下，与
 * {@code platform.access}、{@code platform.invite} 等自助注册<strong>流程</strong> endpoint 分离。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.service.SystemSettingService
 */
package com.innospots.nexus.platform.settings;
