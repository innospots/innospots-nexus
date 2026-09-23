package com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission;

/**
 * 未授予访问权限时的拒绝行为。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum PermissionDenied {

    /** 隐藏受保护的 UI 元素。 */
    HIDDEN,

    /** 显示元素但禁止交互。 */
    DISABLED,

    /** 以只读模式显示元素。 */
    READONLY
}
