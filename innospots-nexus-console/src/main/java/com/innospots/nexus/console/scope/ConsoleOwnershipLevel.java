package com.innospots.nexus.console.scope;

/**
 * 控制台数据默认归属层级（业务可见性与会话要求）。
 */
public enum ConsoleOwnershipLevel {

    /**
     * 租户级数据（如字典）；需要已选租户，不要求工作区。
     */
    TENANT,

    /**
     * 工作区级数据（如角色、菜单、授权）；需要已选工作区。
     */
    WORKSPACE
}
