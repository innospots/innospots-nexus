package com.innospots.nexus.core.persistence.scope;

/**
 * 持久化行归属层级（与控制台 {@code RoleOwnerType} 名称一致）。
 */
public enum OwnerType {

    PLATFORM,
    TENANT,
    WORKSPACE
}
