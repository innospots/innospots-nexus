package com.innospots.nexus.console.dictionary.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 管理控制台字典类型视图。
 *
 * @author Smars
 * @date 2026/09/13
 * @param dictionaryTypeId type 标识符
 * @param typeCode         稳定的类型编码
 * @param typeName         显示名称
 * @param securityRealm    PLATFORM 或 TENANT
 * @param status           生命周期状态
 * @param sortOrder        显示顺序
 * @param builtIn          类型是否由系统管理
 * @param createdAt        创建时间
 * @param updatedAt        最后更新时间
 */
public record DictionaryTypeVo(
        String dictionaryTypeId,
        String typeCode,
        String typeName,
        SecurityRealm securityRealm,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
