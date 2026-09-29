package com.innospots.nexus.core.plugin.installation.domain.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 插件定义在当前有效目录中的存在性。
 * @author Smars
 * @date 2026/09/13
 */
@Schema(description = "插件安装存在状态", enumeration = {"PRESENT", "MISSING"})
public enum PluginPresence {

    /** 定义在当前有效目录中可发现。 */
    PRESENT,

    /** 定义曾安装但当前目录中不可发现。 */
    MISSING
}
