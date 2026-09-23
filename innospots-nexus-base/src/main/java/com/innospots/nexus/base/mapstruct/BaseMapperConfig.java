package com.innospots.nexus.base.mapstruct;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * 所有领域 Mapper 共享的 MapStruct 配置。
 * 配置仅访问器集合映射、始终启用空值检查，并静默忽略未映射的目标字段。
 *
 * @author Smars
 * @date 2026/09/13
 * @see BaseBeanConverter
 */
@MapperConfig(
        collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BaseMapperConfig {
}
