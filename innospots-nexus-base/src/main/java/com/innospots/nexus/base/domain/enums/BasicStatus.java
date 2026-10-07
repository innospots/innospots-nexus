package com.innospots.nexus.base.domain.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 跨领域实体通用的启用/禁用状态。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Schema(description = "启用/禁用状态", enumeration = {"ENABLED", "DISABLED"})
public enum BasicStatus {
    /** 已启用 */
    ENABLED,
    /** 已禁用 */
    DISABLED
}
