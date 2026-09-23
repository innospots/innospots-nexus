package com.innospots.nexus.console.dictionary.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 管理控制台字典项视图。
 *
 * @author Smars
 * @date 2026/09/13
 * @param dictionaryItemId item 标识符
 * @param typeCode         父类型编码
 * @param itemValue        稳定的字典项值
 * @param itemName         显示名称
 * @param securityRealm    PLATFORM 或 TENANT
 * @param status           生命周期状态
 * @param sortOrder        显示顺序
 * @param builtIn          字典项是否由系统管理
 * @param createdAt        创建时间
 * @param updatedAt        最后更新时间
 */
public record DictionaryItemVo(
        String dictionaryItemId,
        String typeCode,
        String itemValue,
        String itemName,
        SecurityRealm securityRealm,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
