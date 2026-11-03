package com.innospots.nexus.platform.settings.domain.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 控制台主导航布局模式。
 *
 * <ul>
 *   <li>{@link #RAIL_DUAL} — 左轨 + 模块侧栏（双栏）</li>
 *   <li>{@link #SINGLE_GROUP} — 单栏分组菜单</li>
 * </ul>
 */
@Schema(description = "控制台导航布局模式")
public enum PlatformNavigationLayoutMode {

    RAIL_DUAL,
    SINGLE_GROUP
}
