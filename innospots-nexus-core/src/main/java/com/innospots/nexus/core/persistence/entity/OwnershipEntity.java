package com.innospots.nexus.core.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * 控制台业务归属列：通过 {@code ownerType} / {@code ownerId} / {@code securityRealm}
 * 表达可见性与存储分区；查询与写入见 {@link com.innospots.nexus.core.persistence.scope.OwnershipScope}。
 *
 * @see com.innospots.nexus.console.scope.ConsoleOwnership
 */
@Getter
@Setter
@MappedSuperclass
public abstract class OwnershipEntity extends BaseEntity {

    /**
     * 归属层级：PLATFORM、TENANT 或 WORKSPACE（与 {@code RoleOwnerType} 名称一致）。
     */
    @Column(length = 32, nullable = false)
    private String ownerType;

    /**
     * 与 {@code ownerType} 匹配的归属标识符；PLATFORM 时为空。
     */
    @Column(length = 32)
    private String ownerId;

    /**
     * 安全域：PLATFORM 或 TENANT。
     */
    @Column(length = 32, nullable = false)
    private String securityRealm;
}
