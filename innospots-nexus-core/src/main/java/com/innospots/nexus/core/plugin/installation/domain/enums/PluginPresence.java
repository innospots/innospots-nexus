package com.innospots.nexus.core.plugin.installation.domain.enums;

/** 插件定义在当前有效目录中的存在性。 */
public enum PluginPresence {

    /** 定义在当前有效目录中可发现。 */
    PRESENT,

    /** 定义曾安装但当前目录中不可发现。 */
    MISSING
}
